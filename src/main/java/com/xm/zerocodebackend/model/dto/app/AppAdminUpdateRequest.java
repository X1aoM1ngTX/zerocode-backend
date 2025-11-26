package com.xm.zerocodebackend.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用管理员更新请求
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@Data
public class AppAdminUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 优先级
     */
    private Integer priority;

    private static final long serialVersionUID = 114514L;
}
