package com.rainbowforest.orderservice.service;

import com.rainbowforest.orderservice.domain.Order;
import java.util.List;

public interface OrderService {
    Order saveOrder(Order order);

    List<Order> getAllOrders();

    Order getOrderById(Long id);

    List<Order> getOrdersByUserId(Long userId);

    Order updateOrderStatus(Long id, String status);

    void deleteOrder(Long id);
}