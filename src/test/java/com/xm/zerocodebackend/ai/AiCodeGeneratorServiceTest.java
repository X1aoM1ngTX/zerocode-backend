package com.xm.zerocodebackend.ai;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.xm.zerocodebackend.ai.model.HtmlCodeResult;
import com.xm.zerocodebackend.ai.model.MultiFileCodeResult;
import com.xm.zerocodebackend.model.enums.CodeGenTypeEnum;

import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

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

    @Test
    void generateReactProjectCodeStream() {
        // 使用注入的工厂方法获取React项目服务实例
        AiCodeGeneratorService reactService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(1L,
                CodeGenTypeEnum.REACT_PROJECT);

        TokenStream tokenStream = reactService.generateReactProjectCodeStream(1L,
                "生成一个React项目，包含一个显示'Hello, World!'的页面，代码量不超过100行");
        Assertions.assertNotNull(tokenStream);
    }

    @Test
    void testChatMemory() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个程序员鱼皮的工具网站，总代码量不超过 20 行");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateHtmlCode("不要生成网站，告诉我你刚刚做了什么？");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateHtmlCode("做个程序员鱼皮的工具网站，总代码量不超过 20 行");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateHtmlCode("不要生成网站，告诉我你刚刚做了什么？");
        Assertions.assertNotNull(result);
    }

}
