package com.xm.zerocodebackend.model.dto.app;

import java.io.Serializable;

import lombok.Data;

/**
 * 应用更新请求
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 114514L;
}