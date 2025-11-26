package com.xm.zerocodebackend.controller;

import com.xm.zerocodebackend.common.BaseResponse;
import com.xm.zerocodebackend.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康控制器
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "系统健康检查服务")
public class HealthController {

    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success( "ok");
    }
}
