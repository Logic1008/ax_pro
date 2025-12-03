package com.axiang.ax_pro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * 应用启动入口
 * 负责引导 Spring Boot 应用启动。
 */
public class DemoApplication {

    /**
     * 主函数入口
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
