package com.xm.vexorbackend.ai.model.message;

import lombok.Getter;

/**
 * 流式消息类型枚举
 */
@Getter
public enum StreamMessageTypeEnum {

    AI_RESPONSE("ai_response", "AI响应"),
    TOOL_REQUEST("tool_request", "工具请求"),
    TOOL_EXECUTED("tool_executed", "工具执行结果"),
    ASSISTANT_MESSAGE("assistant_message", "AI文本响应"),
    TOOL_CALL("tool_call", "工具过程提示"),
    FILE_START("file_start", "文件开始生成"),
    FILE_DELTA("file_delta", "文件内容更新"),
    FILE_DONE("file_done", "文件生成完成"),
    FILE_DELETE("file_delete", "文件删除"),
    BUILD_STATUS("build_status", "构建状态"),
    PREVIEW_READY("preview_ready", "预览可用"),
    ERROR("generation_error", "错误"),
    DONE("done", "完成");

    private final String value;
    private final String text;

    StreamMessageTypeEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据值获取枚举
     */
    public static StreamMessageTypeEnum getEnumByValue(String value) {
        for (StreamMessageTypeEnum typeEnum : values()) {
            if (typeEnum.getValue().equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}
