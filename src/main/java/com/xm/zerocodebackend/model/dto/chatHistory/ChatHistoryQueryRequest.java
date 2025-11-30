package com.xm.zerocodebackend.model.dto.chatHistory;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.xm.zerocodebackend.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对话历史查询请求
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 消息内容（模糊查询）
     */
    private String message;

    /**
     * 消息类型：user/ai/error
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 游标查询 - 最后一条记录的创建时间
     * 用于分页查询，获取早于此时间的记录
     */
    private LocalDateTime lastCreateTime;

    private static final long serialVersionUID = 114514L;
}