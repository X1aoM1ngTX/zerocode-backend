package com.xm.zerocodebackend.model.dto.user;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 114514L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}
