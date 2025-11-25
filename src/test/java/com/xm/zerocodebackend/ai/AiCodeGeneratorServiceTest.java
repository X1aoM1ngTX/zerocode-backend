package com.xm.zerocodebackend.ai;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.xm.zerocodebackend.ai.model.HtmlCodeResult;
import com.xm.zerocodebackend.ai.model.MultiFileCodeResult;

import jakarta.annotation.Resource;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("写一个最基础的页面，不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("写一个最基础的页面，不超过50行");
        Assertions.assertNotNull(multiFileCode);
    }
}
