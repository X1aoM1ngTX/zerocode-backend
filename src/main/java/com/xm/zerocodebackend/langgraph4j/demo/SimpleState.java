package com.xm.zerocodebackend.langgraph4j.demo;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 简单状态
 * 用于存储图的状态
 */
class SimpleState extends AgentState {

    // 消息键
    public static final String MESSAGES_KEY = "messages";

    // 定义状态的模式。
    // MESSAGES_KEY 将保存一个字符串列表，并且会向其中追加新消息。
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES_KEY, Channels.appender(ArrayList::new));

    // 构造函数
    public SimpleState(Map<String, Object> initData) {
        super(initData);
    }

    // 获取消息列表
    public List<String> messages() {
        return this.<List<String>>value("messages")
                .orElse(List.of());
    }
}