package com.xm.zerocodebackend.exception;

import lombok.Getter;

import java.io.Serializable;

/**
 * 自定义业务异常类
 *
 * @author X1aoM1ngTX
 */
@Getter
public class BusinessException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -7034897190745766939L;

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误描述
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getErrorCode();
        this.description = errorCode.getDescription();
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.description = message;
    }

    /**
     * 构造函数
     *
     * @param errorCode   错误码
     * @param description 描述
     */
    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getErrorCode();
        this.description = description;
    }

    /**
     * 构造函数
     *
     * @param errorCode   错误码
     * @param description 描述
     */
    public BusinessException(ErrorCode errorCode, String message, String description) {
        super(message);
        this.code = errorCode.getErrorCode();
        this.description = description;
    }

    /**
     * 获取完整的错误信息
     *
     * @return 完整的错误信息
     */
    public String getDetailMessage() {
        return String.format("错误码: %d, 错误信息: %s, 描述: %s",
                code, getMessage(), description);
    }
}
