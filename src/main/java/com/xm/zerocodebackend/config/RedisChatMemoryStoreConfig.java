package com.xm.zerocodebackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 持久化配置类
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {

    private String host;

    private int port;

    private String password;

    private long ttl;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        log.info("=== Redis连接诊断开始 ===");
        log.info("创建Redis聊天记忆存储，host: {}, port: {}, password: {}, ttl: {}秒", host, port,
                password != null ? "***已设置***" : "未设置", ttl);
        log.info("当前激活的Spring Profile: {}", System.getProperty("spring.profiles.active"));
        log.info("Redis连接详情 - host: {}, port: {}, 密码长度: {}", host, port,
                password != null ? password.length() : 0);
        
        try {
            RedisChatMemoryStore.Builder builder = RedisChatMemoryStore.builder()
                    .host(host)
                    .port(port)
                    .password(password)
                    .ttl(ttl);
            
            // 修复：当设置了密码但没有设置用户名时，指定默认用户名
            if (password != null && !password.trim().isEmpty()) {
                builder.user("default");
                log.info("检测到Redis密码配置，设置默认用户名: default");
            }
            
            RedisChatMemoryStore store = builder.build();
            log.info("Redis聊天记忆存储创建成功");
            log.info("=== Redis连接诊断结束 ===");
            return store;
        } catch (Exception e) {
            log.error("=== Redis连接失败诊断 ===");
            log.error("创建Redis聊天记忆存储失败，host: {}, port: {}, password: {}, error: {}", host, port,
                    password != null ? "***已设置***" : "未设置", e.getMessage(), e);
            log.error("异常类型: {}", e.getClass().getSimpleName());
            log.error("完整错误信息: {}", e.getMessage());
            log.error("=== Redis连接诊断结束 ===");
            throw e;
        }
    }
}
