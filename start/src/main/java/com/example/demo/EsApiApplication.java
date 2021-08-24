package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class EsApiApplication {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${server.port}")
    private String port;

    public static void main(String[] args) {
        SpringApplication.run(EsApiApplication.class, args);
    }

    /**
     * 启动成功
     */
    @Bean
    public ApplicationRunner applicationRunner() {
        return applicationArguments -> {
            try {
                log.info("启动成功：{}", "http://localhost:" + port + contextPath);
            } catch (Exception e) {
                // 输出到日志文件中
                log.error("{}", e.getMessage());
            }
        };
    }

}
