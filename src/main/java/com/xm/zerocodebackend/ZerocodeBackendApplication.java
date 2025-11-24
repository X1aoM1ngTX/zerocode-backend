package com.xm.zerocodebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xm.zerocodebackend.mapper")
public class ZerocodeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZerocodeBackendApplication.class, args);
    }

}
