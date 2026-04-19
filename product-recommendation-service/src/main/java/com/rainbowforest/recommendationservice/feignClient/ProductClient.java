package com.rainbowforest.recommendationservice.feignClient;

import com.rainbowforest.recommendationservice.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Lưu ý: Nếu chạy trong môi trường Microservices thật (có Eureka), 
// bạn nên bỏ phần url="http://localhost:8810/" để Feign tự load balance qua Eureka.
@FeignClient(name = "product-catalog-service") 
public interface ProductClient {

    @GetMapping(value = "/products/{id}")
    // Khai báo rõ tên biến "id" trong @PathVariable
    Product getProductById(@PathVariable("id") Long id);
}