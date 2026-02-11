package com.springsciyon.business.rag;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用主类
 */
@SpringBootApplication
@MapperScan("com.springsciyon.business.rag.dao")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("===========================================");
        System.out.println("Tag API 服务已启动");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("健康检查: http://localhost:8080/api/tags/health");
        System.out.println("===========================================");
    }
}
