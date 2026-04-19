package com.rainbowforest.orderservice.service;

import com.rainbowforest.orderservice.domain.Order;
import com.rainbowforest.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach; // JUnit 5
import org.junit.jupiter.api.Test; // JUnit 5
import org.junit.jupiter.api.extension.ExtendWith; // JUnit 5
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension; // JUnit 5 Mockito Support

import static org.junit.jupiter.api.Assertions.assertEquals; // JUnit 5 Assertions
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Sử dụng Extension thay cho RunWith
class OrderServiceTests { // JUnit 5 không cần public class

    private final Long ORDER_ID = 1L;
    private final String ORDER_STATUS = "testStatus";
    private Order order;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach // Thay cho @Before
    void setUp() {
        order = new Order();
        order.setId(ORDER_ID);
        order.setStatus(ORDER_STATUS);
    }

    @Test
    void save_order_test() {
        // given
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when
        Order created = orderService.saveOrder(order);

        // then
        assertEquals(ORDER_ID, created.getId());
        assertEquals(ORDER_STATUS, created.getStatus());
        
        verify(orderRepository, times(1)).save(any(Order.class));
        verifyNoMoreInteractions(orderRepository);
    }
}