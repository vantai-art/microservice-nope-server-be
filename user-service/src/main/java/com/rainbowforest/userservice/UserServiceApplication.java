package com.rainbowforest.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

// Thêm tham số exclude để Spring Boot bỏ qua việc tự động khóa API
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableFeignClients 
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}