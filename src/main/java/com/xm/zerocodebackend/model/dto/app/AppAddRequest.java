package com.xm.zerocodebackend.model.dto.app;

import java.io.Serializable;

import lombok.Data;

/**
 * 应用添加请求
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    private static final long serialVersionUID = 114514L;
}