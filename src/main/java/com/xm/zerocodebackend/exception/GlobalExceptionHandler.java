package com.xm.zerocodebackend.exception;

import com.xm.zerocodebackend.common.BaseResponse;
import com.xm.zerocodebackend.common.ResultUtils;

//import cn.dev33.satoken.exception.NotLoginException;
//import cn.dev33.satoken.exception.NotPermissionException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author X1aoM1ngTX
 */

@RestControllerAdvice
@Hidden
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 响应
     */
    @SuppressWarnings("rawtypes")
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e) {
        log.error("BusinessException: " + e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());

    }

    /**
     * 处理运行时异常
     *
     * @param e 运行时异常
     * @return 响应
     */
    @SuppressWarnings("rawtypes")
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }

//    /**
//     * 处理Sa-Token未登录异常
//     *
//     * @param e 未登录异常
//     * @return 响应
//     */
//    @ExceptionHandler(NotLoginException.class)
//    public BaseResponse<?> notLoginException(NotLoginException e) {
//        log.error("NotLoginException", e);
//        return ResultUtils.error(ErrorCode.NOT_LOGIN, e.getMessage(), "");
//    }
//
//    /**
//     * 处理Sa-Token无权限异常
//     *
//     * @param e 无权限异常
//     * @return 响应
//     */
//    @ExceptionHandler(NotPermissionException.class)
//    public BaseResponse<?> notPermissionExceptionHandler(NotPermissionException e) {
//        log.error("NotPermissionException", e);
//        return ResultUtils.error(ErrorCode.NO_AUTH, e.getMessage(), "");
//    }

}
