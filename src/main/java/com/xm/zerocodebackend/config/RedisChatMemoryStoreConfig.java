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
        log.info("创建Redis聊天记忆存储，host: {}, port: {}, ttl: {}秒", host, port, ttl);
        try {
            RedisChatMemoryStore store = RedisChatMemoryStore.builder()
                    .host(host)
                    .port(port)
                    .password(password)
                    .ttl(ttl)
                    .build();
            log.info("Redis聊天记忆存储创建成功");
            return store;
        } catch (Exception e) {
            log.error("创建Redis聊天记忆存储失败，host: {}, port: {}, error: {}", host, port, e.getMessage(), e);
            throw e;
        }
    }
}
