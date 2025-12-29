package com.xm.zerocodebackend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 提示词优化服务工厂
 * 负责创建和管理提示词优化服务实例
 */
@Slf4j
@Configuration
public class PromptOptimizerServiceFactory {

    @Resource(name = "promptOptimizerChatModel")
    private ChatModel chatModel;

    /**
     * 创建 AI 提示词优化服务
     *
     * @return 提示词优化服务实例
     */
    @Bean
    public PromptOptimizerService promptOptimizerService() {
        log.info("创建提示词优化服务实例");
        return AiServices.builder(PromptOptimizerService.class)
                .chatModel(chatModel)
                .build();
    }
}
