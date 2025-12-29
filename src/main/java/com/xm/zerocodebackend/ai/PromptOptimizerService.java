package com.xm.zerocodebackend.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * AI 提示词优化服务
 * 用于优化和改写用户的自然语言提示词，使其更适合代码生成
 */
public interface PromptOptimizerService {

    /**
     * 优化用户提示词
     *
     * @param userPrompt 用户的原始提示词
     * @return 优化后的提示词
     */
    @SystemMessage(fromResource = "prompt/prompt-optimizer-system-prompt.txt")
    String optimizePrompt(@UserMessage String userPrompt);
}
