package com.rainbowforest.orderservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

// Sửa javax thành jakarta cho Cookie
import jakarta.servlet.http.Cookie; 

import org.junit.jupiter.api.BeforeEach; // JUnit 5
import org.junit.jupiter.api.Test; // JUnit 5
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.rainbowforest.orderservice.domain.Item;
import com.rainbowforest.orderservice.domain.Order;
import com.rainbowforest.orderservice.domain.Product;
import com.rainbowforest.orderservice.domain.User;
import com.rainbowforest.orderservice.feignclient.UserClient;
import com.rainbowforest.orderservice.service.CartService;
import com.rainbowforest.orderservice.service.OrderService;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest { // JUnit 5 không cần public và @RunWith

    private static final String PRODUCT_NAME = "test";
    private static final Long PRODUCT_ID = 5L;
    private static final Long USER_ID = 1L;
    private static final String USER_NAME = "Test";
    private static final String CART_ID = "1";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CartService cartService;

    @Test
    void save_order_controller_should_return201_when_valid_request() throws Exception {
        // given
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setProductName(PRODUCT_NAME);

        User user = new User();
        user.setUserName(USER_NAME);

        Item item = new Item();
        item.setProduct(product);
        item.setQuantity(1);
        List<Item> cart = new ArrayList<>();
        cart.add(item);

        Order order = new Order();
        order.setItems(cart);
        order.setUser(user);

        // Sử dụng cookie với tên "Cookie" để khớp với @RequestHeader trong Controller
        Cookie cookie = new Cookie("Cookie", CART_ID);

        // when
        when(cartService.getAllItemsFromCart(anyString())).thenReturn(cart);
        when(userClient.getUserById(anyLong())).thenReturn(user);
        when(orderService.saveOrder(any(Order.class))).thenReturn(order);

        // then
        mockMvc.perform(post("/order/{userId}", USER_ID)
                .cookie(cookie))
                .andExpect(status().isCreated())
                // Lưu ý: Đã bỏ .UTF8 vì MediaType.APPLICATION_JSON trong Boot 3 mặc định là UTF-8
                .andExpect(jsonPath("$.items").isArray());

        verify(orderService, times(1)).saveOrder(any(Order.class));
        verifyNoMoreInteractions(orderService);
    }
}