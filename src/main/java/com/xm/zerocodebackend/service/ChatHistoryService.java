package com.xm.zerocodebackend.service;

import java.time.LocalDateTime;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xm.zerocodebackend.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.xm.zerocodebackend.model.entity.ChatHistory;
import com.xm.zerocodebackend.model.entity.User;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话消息
     * 
     * @param appId       应用 ID
     * @param message     消息内容
     * @param messageType 消息类型
     * @param userId      用户 ID
     * @return 是否添加成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据应用 ID 删除对话历史
     * 
     * @param appId 应用 ID
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 根据应用 ID 分页查询对话历史
     * 
     * @param appId          应用 ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后创建时间游标
     * @param loginUser      当前登录用户
     * @return 对话历史分页数据
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

    /**
     * 加载对话历史到内存
     *
     * @param appId
     * @param chatMemory
     * @param maxCount   最多加载多少条
     * @return 加载成功的条数
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
