package com.xm.zerocodebackend.ai;

import dev.langchain4j.model.chat.StreamingChatModel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xm.zerocodebackend.ai.tools.ToolManager;
import com.xm.zerocodebackend.exception.BusinessException;
import com.xm.zerocodebackend.exception.ErrorCode;
import com.xm.zerocodebackend.model.enums.CodeGenTypeEnum;
import com.xm.zerocodebackend.service.ChatHistoryService;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel openAiStreamingChatModel;

    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，cacheKey: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据 appId 获取服务（带缓存）
     * 这个方法是为了兼容历史逻辑
     *
     * @param appId 应用 ID
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 根据 appId 和代码生成类型获取服务（带缓存）
     *
     * @param appId       应用 ID
     * @param codeGenType 代码生成类型
     * @return AI 服务实例
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        log.debug("尝试获取AI服务实例，appId: {}, codeGenType: {}, cacheKey: {}", appId, codeGenType, cacheKey);

        return serviceCache.get(cacheKey, key -> {
            log.info("缓存中未找到AI服务实例，开始创建新实例，appId: {}, codeGenType: {}", appId, codeGenType);
            return createAiCodeGeneratorService(appId, codeGenType);
        });
    }

    /**
     * 创建新的 AI 服务实例
     * 
     * @param appId       应用 ID
     * @param codeGenType 代码生成类型
     * @return AI 服务实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        log.info("开始创建AI服务实例，appId: {}, codeGenType: {}", appId, codeGenType);

        try {
            // 根据 appId 构建独立的对话记忆
            log.info("为 appId: {} 创建聊天记忆，使用Redis存储", appId);
            MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                    .builder()
                    .id(appId)
                    .chatMemoryStore(redisChatMemoryStore)
                    .maxMessages(60)
                    .build();

            log.info("聊天记忆创建完成，开始从数据库加载历史对话到Redis");
            // 从数据库加载历史对话到记忆中
            int loadedCount = chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
            log.info("历史对话加载完成，共加载 {} 条消息", loadedCount);

            // 根据代码生成类型选择不同的模型配置
            AiCodeGeneratorService service = switch (codeGenType) {
                // Vue 项目生成使用推理模型
                case VUE_PROJECT -> {
                    log.info("为 appId: {} 创建Vue项目生成服务，使用推理模型", appId);
                    yield AiServices.builder(AiCodeGeneratorService.class)
                            .streamingChatModel(reasoningStreamingChatModel)
                            .chatMemoryProvider(memoryId -> chatMemory)
                            .tools(toolManager.getAllTools())
                            .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                                    toolExecutionRequest,
                                    "Error: there is no tool called " + toolExecutionRequest.name()))
                            .build();
                }
                // HTML 和多文件生成使用默认模型
                case HTML, MULTI_FILE -> {
                    log.info("为 appId: {} 创建{}生成服务，使用默认模型", appId, codeGenType.getValue());
                    yield AiServices.builder(AiCodeGeneratorService.class)
                            .chatModel(chatModel)
                            .streamingChatModel(openAiStreamingChatModel)
                            .chatMemory(chatMemory)
                            .build();
                }
                default -> throw new BusinessException(ErrorCode.UNSUPPORTED_TYPE,
                        "不支持的代码生成类型: " + codeGenType.getValue());
            };

            log.info("AI服务实例创建成功，appId: {}, codeGenType: {}", appId, codeGenType);
            return service;

        } catch (Exception e) {
            log.error("创建AI服务实例失败，appId: {}, codeGenType: {}, error: {}", appId, codeGenType, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 创建 AI 代码生成器服务
     *
     * @return 默认 AI 服务实例（appId 为 0）
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0);
    }

    /**
     * 构建缓存键
     *
     * @param appId       应用 ID
     * @param codeGenType 代码生成类型
     * @return 缓存键
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }
}
