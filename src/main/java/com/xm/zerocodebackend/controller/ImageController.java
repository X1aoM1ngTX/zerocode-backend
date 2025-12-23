package com.xm.zerocodebackend.controller;

import com.xm.zerocodebackend.common.BaseResponse;
import com.xm.zerocodebackend.common.ResultUtils;
import com.xm.zerocodebackend.model.dto.image.ImageGenerateRequest;
import com.xm.zerocodebackend.model.dto.image.ImageGenerateResponse;
import com.xm.zerocodebackend.service.ZImageTurboService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图像生成控制器
 * API 文档地址：https://bailian.console.aliyun.com/?tab=api#/api/?type=model&url=3002354
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@RestController
@RequestMapping("/image")
@Tag(name = "Image", description = "图像生成服务")
public class ImageController {

    @Resource
    private ZImageTurboService zImageTurboService;

    /**
     * 使用 Z-Image-Turbo 生成图像
     *
     * @param request 图像生成请求
     * @return 图像生成响应
     */
    @Operation(summary = "生成图像", description = "使用阿里云 Z-Image-Turbo 模型生成图像")
    @PostMapping("/generate")
    public BaseResponse<ImageGenerateResponse> generateImage(@Valid @RequestBody ImageGenerateRequest request) {
        ImageGenerateResponse response = zImageTurboService.generateImage(request);
        return ResultUtils.success(response);
    }
}
