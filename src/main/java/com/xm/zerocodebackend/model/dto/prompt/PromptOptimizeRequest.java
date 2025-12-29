package com.xm.zerocodebackend.model.dto.prompt;

import lombok.Data;

import java.io.Serializable;

/**
 * 提示词优化请求
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@Data
public class PromptOptimizeRequest implements Serializable {

    /**
     * 用户原始提示词
     */
    private String originalPrompt;

    private static final long serialVersionUID = 1L;
}
