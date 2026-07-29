package com.xm.vexorbackend.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * AI 应用名称生成服务
 * 根据用户初始需求生成简短、可读的应用名称
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
public interface AppNameGeneratorService {

    /**
     * 生成应用名称
     *
     * @param userPrompt 用户初始需求
     * @return 应用名称
     */
    @SystemMessage(fromResource = "prompt/app-name-generator-system-prompt.txt")
    String generateAppName(@UserMessage String userPrompt);
}
