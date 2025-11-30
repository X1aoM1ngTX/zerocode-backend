package com.xm.zerocodebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.xm.zerocodebackend.mapper")
public class ZerocodeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZerocodeBackendApplication.class, args);
    }

}
