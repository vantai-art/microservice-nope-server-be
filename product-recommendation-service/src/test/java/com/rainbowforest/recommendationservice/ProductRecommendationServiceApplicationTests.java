package com.rainbowforest.recommendationservice;

import org.junit.jupiter.api.Test; // Chuyển từ org.junit sang org.junit.jupiter
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductRecommendationServiceApplicationTests { // JUnit 5 không cần public class

    @Test
    void contextLoads() {
        // Test này sẽ pass nếu toàn bộ Spring Context (Bean, JPA, Feign, Discovery Client) 
        // khởi tạo thành công mà không gặp lỗi cấu hình.
    }

}