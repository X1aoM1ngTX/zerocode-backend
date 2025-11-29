package com.xm.zerocodebackend.model.dto.app;

import java.io.Serializable;

import lombok.Data;

@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    private static final long serialVersionUID = 114514L;
}
