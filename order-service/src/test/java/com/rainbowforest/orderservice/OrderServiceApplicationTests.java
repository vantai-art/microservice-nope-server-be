package com.rainbowforest.orderservice;

import org.junit.jupiter.api.Test; // JUnit 5
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceApplicationTests { // JUnit 5 không cần public và không cần @RunWith

    @Test
    void contextLoads() {
        // Kiểm tra xem toàn bộ các Bean (Redis, JPA, Feign) có khởi tạo thành công không
    }

}