package com.rainbowforest.recommendationservice.feignClient;

import com.rainbowforest.recommendationservice.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client để gọi sang User Service (Port 8811)
 * Lưu ý: Nên dùng tên service khớp với spring.application.name của User Service
 */
@FeignClient(name = "user-service") 
public interface UserClient {

    @GetMapping(value = "/users/{id}")
    // Đảm bảo khai báo rõ ràng "id" trong PathVariable cho Spring Boot 3
    User getUserById(@PathVariable("id") Long id);
}