package com.xm.zerocodebackend.langgraph4j.demo;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.List;
import java.util.Map;

/**
 * 问候节点
 * 用于向图中添加问候消息
 */
class GreeterNode implements NodeAction<SimpleState> {
    
    @Override
    public Map<String, Object> apply(SimpleState state) {
        System.out.println("执行问候语节点。当前信息：" + state.messages());
        return Map.of(SimpleState.MESSAGES_KEY, "你好，问候语节点!");
    }
}

/**
 * 响应节点
 * 用于响应问候节点的消息
 */
class ResponderNode implements NodeAction<SimpleState> {

    @Override
    public Map<String, Object> apply(SimpleState state) {
        System.out.println("执行响应节点。当前信息：" + state.messages());
        List<String> currentMessages = state.messages();
        if (currentMessages.contains("你好，问候语节点!")) {
            return Map.of(SimpleState.MESSAGES_KEY, "已确认问候!");
        }
        return Map.of(SimpleState.MESSAGES_KEY, "未找到问候。");
    }
}