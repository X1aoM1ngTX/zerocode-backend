package com.xm.zerocodebackend.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 提示词优化 AI 模型配置
 * 返回纯文本而非 JSON 格式
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.prompt-optimizer-chat-model")
@Data
public class PromptOptimizerChatModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private int maxTokens;

    private Double temperature;

    private Boolean logRequests = false;

    private Boolean logResponses = false;

    private Duration timeout;

    /**
     * 创建用于提示词优化的 ChatModel
     * 不使用 response-format，返回纯文本
     */
    @Bean
    public ChatModel promptOptimizerChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .timeout(timeout)
                .build();
    }
}
