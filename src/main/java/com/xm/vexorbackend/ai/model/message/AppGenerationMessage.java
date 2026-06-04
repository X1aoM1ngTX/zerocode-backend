package com.xm.vexorbackend.ai.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 应用生成流式事件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AppGenerationMessage extends StreamMessage {

    /** 应用 ID */
    private Long appId;

    /** 文件路径（文件事件使用） */
    private String path;

    /** 文本内容或文件内容 */
    private String content;

    /** 是否覆盖写入 */
    private Boolean overwrite;

    /** 事件状态（generating / done / error / ready） */
    private String status;

    /** 人类可读的提示信息 */
    private String message;

    /** 事件时间戳 */
    private Long timestamp;

    public AppGenerationMessage(String type) {
        super(type);
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * AI 文本响应事件
     * @param appId 应用 ID
     * @param content AI 回复的文本片段
     */
    public static AppGenerationMessage assistantMessage(Long appId, String content) {
        AppGenerationMessage message = new AppGenerationMessage(StreamMessageTypeEnum.ASSISTANT_MESSAGE.getValue());
        message.setAppId(appId);
        message.setContent(content);
        return message;
    }

    /**
     * 工具调用提示事件
     * @param appId 应用 ID
     * @param content 工具调用描述
     */
    public static AppGenerationMessage toolCall(Long appId, String content) {
        AppGenerationMessage message = new AppGenerationMessage(StreamMessageTypeEnum.TOOL_CALL.getValue());
        message.setAppId(appId);
        message.setMessage(content);
        message.setContent(content);
        return message;
    }

    /**
     * 文件开始生成事件
     * @param appId 应用 ID
     * @param path 文件相对路径
     */
    public static AppGenerationMessage fileStart(Long appId, String path) {
        AppGenerationMessage message = new AppGenerationMessage(StreamMessageTypeEnum.FILE_START.getValue());
        message.setAppId(appId);
        message.setPath(path);
        message.setStatus("generating");
        message.setMessage("正在生成 " + path);
        return message;
    }

    /**
     * 文件内容增量更新事件
     * @param appId 应用 ID
     * @param path 文件相对路径
     * @param content 当前累积的文件内容
     * @param overwrite 是否覆盖已有文件
     */
    public static AppGenerationMessage fileDelta(Long appId, String path, String content, boolean overwrite) {
        AppGenerationMessage message = new AppGenerationMessage(StreamMessageTypeEnum.FILE_DELTA.getValue());
        message.setAppId(appId);
        message.setPath(path);
        message.setContent(content);
        message.setOverwrite(overwrite);
        message.setStatus("generating");
        return message;
    }

    /**
     * 文件生成完成事件
     * @param appId 应用 ID
     * @param path 文件相对路径
     */
    public static AppGenerationMessage fileDone(Long appId, String path) {
        AppGenerationMessage message = new AppGenerationMessage(StreamMessageTypeEnum.FILE_DONE.getValue());
        message.setAppId(appId);
        message.setPath(path);
        message.setStatus("done");
        message.setMessage(path + " 已生成");
        return message;
    }

    /**
     * 构建状态事件（用于 Vue/React 项目构建进度通知）
     * @param appId 应用 ID
     * @param status 构建状态（building / success / error）
     * @param content 状态描述文本
     */
    public static AppGenerationMessage buildStatus(Long appId, String status, String content) {
        AppGenerationMessage message = new AppGenerationMessage(StreamMessageTypeEnum.BUILD_STATUS.getValue());
        message.setAppId(appId);
        message.setStatus(status);
        message.setMessage(content);
        message.setContent(content);
        return message;
    }

    /**
     * 预览就绪事件
     * @param appId 应用 ID
     * @param content 提示信息
     */
    public static AppGenerationMessage previewReady(Long appId, String content) {
        AppGenerationMessage message = new AppGenerationMessage(StreamMessageTypeEnum.PREVIEW_READY.getValue());
        message.setAppId(appId);
        message.setStatus("ready");
        message.setMessage(content);
        message.setContent(content);
        return message;
    }

    /**
     * 错误事件
     * @param appId 应用 ID
     * @param content 错误描述
     */
    public static AppGenerationMessage error(Long appId, String content) {
        AppGenerationMessage message = new AppGenerationMessage(StreamMessageTypeEnum.ERROR.getValue());
        message.setAppId(appId);
        message.setStatus("error");
        message.setMessage(content);
        message.setContent(content);
        return message;
    }
}
