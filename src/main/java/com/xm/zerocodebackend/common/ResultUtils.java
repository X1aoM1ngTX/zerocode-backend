package com.xm.zerocodebackend.common;

import com.xm.zerocodebackend.exception.ErrorCode;

/**
 * 封装返回类
 *
 * @author X1aoM1ngTX
 */
public class ResultUtils {

    /**
     * 返回成功
     *
     * @param data
     * @param <T>  泛型
     * @return
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok", "请求成功");
    }

    /**
     * 返回错误码
     *
     * @param errorCode 错误码
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * @param code        错误码
     * @param message     消息
     * @param description 描述
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse(code, null, message, description);
    }

    /**
     * @param errorCode   错误码
     * @param message     消息
     * @param description 描述
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse(errorCode.getErrorCode(), null, message, description);
    }

    /**
     * @param errorCode   错误码
     * @param description 描述
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse(errorCode.getErrorCode(), null, errorCode.getMessage(), description);
    }
}
