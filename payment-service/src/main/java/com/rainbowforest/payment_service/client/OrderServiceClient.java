package com.rainbowforest.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    // Endpoint này đã được thêm vào OrderController: PUT
    // /order/{orderId}/payment-status
    @PutMapping("/order/{orderId}/payment-status")
    Map<String, Object> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam String status);
}
