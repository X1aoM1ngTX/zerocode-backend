package com.xm.vexorbackend;

import com.xm.vexorbackend.utils.WebScreenshotUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = { RedisEmbeddingStoreAutoConfiguration.class })
@MapperScan("com.xm.vexorbackend.mapper")
@EnableCaching
public class VexorBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(VexorBackendApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread(WebScreenshotUtils::destroy));
    }
}
