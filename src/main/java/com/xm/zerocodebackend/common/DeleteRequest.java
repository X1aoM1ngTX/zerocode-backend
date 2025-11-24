package com.xm.zerocodebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求
 *
 * @author X1aoM1ngTX
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * id
     */
    private Long id;
}
