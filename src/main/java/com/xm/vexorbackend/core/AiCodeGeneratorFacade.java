package com.xm.vexorbackend.core;

import cn.hutool.json.JSONUtil;
import com.xm.vexorbackend.ai.AiCodeGeneratorService;
import com.xm.vexorbackend.ai.AiCodeGeneratorServiceFactory;
import com.xm.vexorbackend.ai.model.HtmlCodeResult;
import com.xm.vexorbackend.ai.model.MultiFileCodeResult;
import com.xm.vexorbackend.ai.model.message.AppGenerationMessage;
import com.xm.vexorbackend.ai.model.message.AiResponseMessage;
import com.xm.vexorbackend.ai.model.message.ToolExecutedMessage;
import com.xm.vexorbackend.ai.model.message.ToolRequestMessage;
import com.xm.vexorbackend.constant.AppConstant;
import com.xm.vexorbackend.core.builder.ReactProjectBuilder;
import com.xm.vexorbackend.core.builder.VueProjectBuilder;
import com.xm.vexorbackend.core.handler.FileBlockStreamParser;
import com.xm.vexorbackend.core.parser.CodeParserExecutor;
import com.xm.vexorbackend.core.saver.CodeFileSaverExecutor;
import com.xm.vexorbackend.exception.BusinessException;
import com.xm.vexorbackend.exception.ErrorCode;
import com.xm.vexorbackend.model.enums.CodeGenTypeEnum;
import com.xm.vexorbackend.service.AppFileService;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 代码生成门面类，组合代码生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ReactProjectBuilder reactProjectBuilder;

    @Resource
    private AppFileService appFileService;

    // -=== 结构化生成代码 ===-

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId           应用 ID
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空", "生成代码时必须指定生成类型");
        }
        // 根据 appId 获取相应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,
                codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.UNSUPPORTED_TYPE, errorMessage, "生成代码时指定的类型不被支持");
            }
        };
    }

    // -=== 流式生成代码 ===-

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId           应用 ID
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空", "生成代码时必须指定生成类型");
        }
        // 根据 appId 获取相应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,
                codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, CodeGenTypeEnum.VUE_PROJECT, appId);
            }
            case REACT_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateReactProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, CodeGenTypeEnum.REACT_PROJECT, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.UNSUPPORTED_TYPE, errorMessage, "生成代码时指定的类型不被支持");
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（事件流 v2）。
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId           应用 ID
     * @return 结构化事件流，前端按 event type 分别处理
     */
    public Flux<AppGenerationMessage> generateAndSaveCodeEventStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,
            Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空", "生成代码时必须指定生成类型");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,
                codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> processCodeStreamWithEvents(
                    aiCodeGeneratorService.generateHtmlCodeStream(userMessage), CodeGenTypeEnum.HTML, appId);
            case MULTI_FILE -> processCodeStreamWithEvents(
                    aiCodeGeneratorService.generateMultiFileCodeStream(userMessage), CodeGenTypeEnum.MULTI_FILE, appId);
            case VUE_PROJECT -> processTokenStreamWithEvents(
                    aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage), CodeGenTypeEnum.VUE_PROJECT,
                    appId);
            case REACT_PROJECT -> processTokenStreamWithEvents(
                    aiCodeGeneratorService.generateReactProjectCodeStream(appId, userMessage),
                    CodeGenTypeEnum.REACT_PROJECT, appId);
        };
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @param codeGenType 代码生成类型
     * @param appId       应用 ID
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, CodeGenTypeEnum codeGenType, Long appId) {
        return Flux.create(sink -> tokenStream
                // 处理部分响应
                .onPartialResponse((String partialResponse) -> {
                    AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                    sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                })
                // 处理工具调用信息
                .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                    ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                    sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                })
                // 处理工具执行完成信息
                .onToolExecuted((ToolExecution toolExecution) -> {
                    ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                    sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                })
                // 处理完整响应
                .onCompleteResponse((ChatResponse response) -> {
                    // 根据代码生成类型选择相应的构建器
                    switch (codeGenType) {
                        case VUE_PROJECT -> {
                            String vueProjectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_"
                                    + appId;
                            vueProjectBuilder.buildProject(vueProjectPath);
                            log.info("Vue 项目构建完成，路径: {}", vueProjectPath);
                        }
                        case REACT_PROJECT -> {
                            String reactProjectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator
                                    + "react_project_" + appId;
                            reactProjectBuilder.buildProject(reactProjectPath);
                            log.info("React 项目构建完成，路径: {}", reactProjectPath);
                        }
                        default -> {
                            log.warn("未知的代码生成类型: {}", codeGenType.getValue());
                        }
                    }
                    sink.complete();
                })
                // 处理错误
                .onError((Throwable error) -> {
                    error.printStackTrace();
                    sink.error(error);
                })
                .start());
    }

    /**
     * 将 TokenStream 转换为 v2 事件流（用于 Vue/React 项目类型）。
     *
     * @param tokenStream LangChain4j TokenStream
     * @param codeGenType 代码生成类型
     * @param appId       应用 ID
     * @return 事件流
     */
    private Flux<AppGenerationMessage> processTokenStreamWithEvents(TokenStream tokenStream, CodeGenTypeEnum codeGenType,
            Long appId) {
        return Flux.create(sink -> tokenStream
                .onPartialResponse((String partialResponse) -> sink
                        .next(AppGenerationMessage.assistantMessage(appId, partialResponse)))
                .onPartialToolExecutionRequest((index, toolExecutionRequest) -> sink
                        .next(AppGenerationMessage.toolCall(appId, "正在执行工具：" + toolExecutionRequest.name())))
                .onToolExecuted((ToolExecution toolExecution) -> {
                    String toolName = toolExecution.request().name();
                    sink.next(AppGenerationMessage.toolCall(appId, "工具执行完成：" + toolName));
                    emitToolFileEvents(appId, toolName, toolExecution.request().arguments(), sink::next);
                })
                .onCompleteResponse((ChatResponse response) -> {
                    try {
                        buildProjectIfNeeded(codeGenType, appId, event -> sink.next(event));
                        sink.next(AppGenerationMessage.previewReady(appId, "预览已更新"));
                        sink.complete();
                    } catch (Exception e) {
                        sink.next(AppGenerationMessage.error(appId, "构建失败：" + e.getMessage()));
                        sink.complete();
                    }
                })
                .onError((Throwable error) -> {
                    sink.next(AppGenerationMessage.error(appId, "AI 回复失败：" + error.getMessage()));
                    sink.complete();
                })
                .start());
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @param appId       应用 ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段
        return codeStream.doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    // 流式返回完成后保存代码
                    try {
                        String completeCode = codeBuilder.toString();
                        // 使用执行器解析代码
                        Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                        // 使用执行器保存代码
                        File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                        log.info("保存成功，路径为：{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败: {}", e.getMessage());
                    }
                });
    }

    /**
     * 普通代码流转 v2 事件，解析 file:path 代码块并实时写入文件。
     *
     * @param codeStream  原始代码流
     * @param codeGenType 代码生成类型
     * @param appId       应用 ID
     * @return 事件流
     */
    private Flux<AppGenerationMessage> processCodeStreamWithEvents(Flux<String> codeStream, CodeGenTypeEnum codeGenType,
            Long appId) {
        FileBlockStreamParser parser = new FileBlockStreamParser(appId);
        StringBuilder completeCode = new StringBuilder();
        AtomicInteger fileDeltaCount = new AtomicInteger(0);
        return codeStream
                .concatMap(chunk -> {
                    completeCode.append(chunk);
                    List<AppGenerationMessage> events = parser.accept(chunk);
                    persistFileDeltaEvents(events, codeGenType, fileDeltaCount);
                    return Flux.fromIterable(events);
                })
                .concatWith(Flux.defer(() -> {
                    List<AppGenerationMessage> events = new ArrayList<>(parser.complete());
                    persistFileDeltaEvents(events, codeGenType, fileDeltaCount);
                    if (fileDeltaCount.get() == 0) {
                        events.addAll(createFallbackFileEvents(completeCode.toString(), codeGenType, appId));
                        persistFileDeltaEvents(events, codeGenType, fileDeltaCount);
                    }
                    events.add(AppGenerationMessage.buildStatus(appId, "success", "代码已保存"));
                    events.add(AppGenerationMessage.previewReady(appId, "预览已更新"));
                    return Flux.fromIterable(events);
                }))
                .onErrorResume(error -> Flux.just(AppGenerationMessage.error(appId, "生成失败：" + error.getMessage())));
    }

    /**
     * 遍历事件列表，将 file_delta 事件对应的文件内容落盘
     */
    private void persistFileDeltaEvents(List<AppGenerationMessage> events, CodeGenTypeEnum codeGenType,
            AtomicInteger fileDeltaCount) {
        for (AppGenerationMessage event : events) {
            if ("file_delta".equals(event.getType())) {
                appFileService.writeFile(event.getAppId(), codeGenType, event.getPath(), event.getContent());
                fileDeltaCount.incrementAndGet();
            }
        }
    }

    /**
     * 当流中没有 file:path 代码块时，使用传统解析器兜底生成文件事件
     */
    private List<AppGenerationMessage> createFallbackFileEvents(String completeCode, CodeGenTypeEnum codeGenType,
            Long appId) {
        List<AppGenerationMessage> events = new ArrayList<>();
        Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
        if (codeGenType == CodeGenTypeEnum.HTML) {
            HtmlCodeResult result = (HtmlCodeResult) parsedResult;
            addFileEvents(events, appId, "index.html", result.getHtmlCode());
        } else if (codeGenType == CodeGenTypeEnum.MULTI_FILE) {
            MultiFileCodeResult result = (MultiFileCodeResult) parsedResult;
            addFileEvents(events, appId, "index.html", result.getHtmlCode());
            addFileEvents(events, appId, "style.css", result.getCssCode());
            addFileEvents(events, appId, "script.js", result.getJsCode());
        }
        return events;
    }

    /** 将单个文件内容包装为 file_start + file_delta + file_done 事件序列 */
    private void addFileEvents(List<AppGenerationMessage> events, Long appId, String path, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        events.add(AppGenerationMessage.toolCall(appId, "正在生成 " + path));
        events.add(AppGenerationMessage.fileStart(appId, path));
        events.add(AppGenerationMessage.fileDelta(appId, path, content, true));
        events.add(AppGenerationMessage.fileDone(appId, path));
    }

    /**
     * 解析工具调用参数，提取 writeFile / modifyFile 的文件路径和内容并发射文件事件
     */
    private void emitToolFileEvents(Long appId, String toolName, String arguments,
            java.util.function.Consumer<AppGenerationMessage> consumer) {
        if (arguments == null || arguments.isBlank()) {
            return;
        }
        try {
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(arguments);
            if ("writeFile".equals(toolName)) {
                String path = jsonObject.getStr("relativeFilePath");
                String content = jsonObject.getStr("content");
                if (path != null) {
                    consumer.accept(AppGenerationMessage.fileStart(appId, path));
                    consumer.accept(AppGenerationMessage.fileDelta(appId, path, content == null ? "" : content, true));
                    consumer.accept(AppGenerationMessage.fileDone(appId, path));
                }
            } else if ("modifyFile".equals(toolName)) {
                String path = jsonObject.getStr("relativeFilePath");
                if (path != null) {
                    String content = appFileService.readFileContent(appId, path);
                    consumer.accept(AppGenerationMessage.fileStart(appId, path));
                    consumer.accept(AppGenerationMessage.fileDelta(appId, path, content, true));
                    consumer.accept(AppGenerationMessage.fileDone(appId, path));
                }
            }
        } catch (Exception e) {
            log.warn("解析工具文件事件失败: {}", e.getMessage());
        }
    }

    /**
     * 根据代码生成类型决定是否需要执行项目构建，并发射构建状态事件
     */
    private void buildProjectIfNeeded(CodeGenTypeEnum codeGenType, Long appId,
            java.util.function.Consumer<AppGenerationMessage> consumer) {
        switch (codeGenType) {
            case VUE_PROJECT -> {
                String vueProjectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;
                consumer.accept(AppGenerationMessage.buildStatus(appId, "building", "正在构建 Vue 项目"));
                boolean success = vueProjectBuilder.buildProject(vueProjectPath);
                consumer.accept(AppGenerationMessage.buildStatus(appId, success ? "success" : "error",
                        success ? "Vue 项目构建完成" : "Vue 项目构建失败"));
            }
            case REACT_PROJECT -> {
                String reactProjectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "react_project_" + appId;
                consumer.accept(AppGenerationMessage.buildStatus(appId, "building", "正在构建 React 项目"));
                boolean success = reactProjectBuilder.buildProject(reactProjectPath);
                consumer.accept(AppGenerationMessage.buildStatus(appId, success ? "success" : "error",
                        success ? "React 项目构建完成" : "React 项目构建失败"));
            }
            default -> {
            }
        }
    }

}
