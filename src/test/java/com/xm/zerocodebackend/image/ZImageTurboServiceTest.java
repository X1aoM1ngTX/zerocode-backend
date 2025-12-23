package com.xm.zerocodebackend.image;

import com.xm.zerocodebackend.model.dto.image.ImageGenerateRequest;
import com.xm.zerocodebackend.model.dto.image.ImageGenerateResponse;
import com.xm.zerocodebackend.service.ZImageTurboService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

/**
 * Z-Image-Turbo 图像生成服务测试
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@SpringBootTest
class ZImageTurboServiceTest {

    @Resource
    private ZImageTurboService zImageTurboService;

    /**
     * 测试基本图像生成
     */
    @Test
    void testGenerateImage() {
        ImageGenerateRequest request = new ImageGenerateRequest();
        request.setUserPrompt("一只坐着的橘黄色的猫，表情愉悦，活泼可爱，逼真准确。");
        request.setSize("1024*1536");
        request.setPromptExtend(false);

        ImageGenerateResponse response = zImageTurboService.generateImage(request);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getImageUrl());
        Assertions.assertNotNull(response.getRequestId());
        System.out.println("图像 URL: " + response.getImageUrl());
        System.out.println("请求 ID: " + response.getRequestId());
        System.out.println("图像尺寸: " + response.getWidth() + "x" + response.getHeight());
    }

    /**
     * 测试带负向提示词的图像生成
     */
    @Test
    void testGenerateImageWithNegativePrompt() {
        ImageGenerateRequest request = new ImageGenerateRequest();
        request.setUserPrompt("一只在花园里玩耍的小狗");
        request.setNegativePrompt("模糊, 低质量, 变形");
        request.setSize("1024*1536");
        request.setPromptExtend(false);

        ImageGenerateResponse response = zImageTurboService.generateImage(request);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getImageUrl());
        System.out.println("图像 URL: " + response.getImageUrl());
    }

    /**
     * 测试带种子的图像生成
     */
    @Test
    void testGenerateImageWithSeed() {
        ImageGenerateRequest request = new ImageGenerateRequest();
        request.setUserPrompt("一只在海滩上玩耍的海鸥");
        request.setSeed(12345);
        request.setSize("1024*1536");
        request.setPromptExtend(false);

        ImageGenerateResponse response = zImageTurboService.generateImage(request);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getImageUrl());
        System.out.println("图像 URL: " + response.getImageUrl());
    }

    /**
     * 测试开启智能提示词改写的图像生成
     */
    @Test
    void testGenerateImageWithPromptExtend() {
        ImageGenerateRequest request = new ImageGenerateRequest();
        request.setUserPrompt("一只在森林里奔跑的鹿");
        request.setPromptExtend(true);
        request.setSize("1024*1536");

        ImageGenerateResponse response = zImageTurboService.generateImage(request);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getImageUrl());
        Assertions.assertNotNull(response.getText()); // 应该包含改写后的提示词
        Assertions.assertNotNull(response.getReasoningContent()); // 应该包含思考过程
        System.out.println("图像 URL: " + response.getImageUrl());
        System.out.println("改写后的提示词: " + response.getText());
        System.out.println("思考过程: " + response.getReasoningContent());
    }

    /**
     * 测试不同分辨率的图像生成
     */
    @Test
    void testGenerateImageWithDifferentSizes() {
        String[] sizes = { "1024*1024", "1280*720", "720*1280" };

        for (String size : sizes) {
            ImageGenerateRequest request = new ImageGenerateRequest();
            request.setUserPrompt("一只飞翔的鸟");
            request.setSize(size);
            request.setPromptExtend(false);

            ImageGenerateResponse response = zImageTurboService.generateImage(request);

            Assertions.assertNotNull(response);
            Assertions.assertNotNull(response.getImageUrl());
            System.out.println("尺寸 " + size + " 的图像 URL: " + response.getImageUrl());
        }
    }
}
