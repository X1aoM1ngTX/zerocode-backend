package com.xm.vexorbackend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 应用名称生成服务工厂
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@Slf4j
@Configuration
public class AppNameGeneratorServiceFactory {

    @Resource(name = "promptOptimizerChatModel")
    private ChatModel chatModel;

    /**
     * 创建 AI 应用名称生成服务
     *
     * @return 应用名称生成服务实例
     */
    @Bean
    public AppNameGeneratorService appNameGeneratorService() {
        log.info("创建应用名称生成服务实例");
        return AiServices.builder(AppNameGeneratorService.class)
                .chatModel(chatModel)
                .build();
    }
}
