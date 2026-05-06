package com.rainbowforest.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;

// FIX: Đã XÓA @EnableRedisWebSession và import của nó
// Lý do: @EnableRedisWebSession bắt Redis phải RUNNING cho MỌI request qua gateway.
// Nếu Redis chưa chạy → mọi request (POST /order/direct, GET /products...) đều 500
// TRƯỚC KHI request đến service đích.
// Gateway chỉ làm nhiệm vụ ROUTING — không cần lưu HTTP session.
// Auth/session do user-service xử lý riêng.
@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        ReactiveSecurityAutoConfiguration.class,
        ReactiveUserDetailsServiceAutoConfiguration.class,
        ReactiveManagementWebSecurityAutoConfiguration.class
})
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}