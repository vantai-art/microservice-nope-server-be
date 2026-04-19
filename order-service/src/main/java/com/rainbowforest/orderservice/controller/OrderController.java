package com.rainbowforest.orderservice.controller;

import com.rainbowforest.orderservice.domain.Item;
import com.rainbowforest.orderservice.domain.Order;
import com.rainbowforest.orderservice.domain.Product;
import com.rainbowforest.orderservice.domain.User;
import com.rainbowforest.orderservice.feignclient.UserClient;
import com.rainbowforest.orderservice.http.header.HeaderGenerator;
import com.rainbowforest.orderservice.service.CartService;
import com.rainbowforest.orderservice.service.OrderService;
import com.rainbowforest.orderservice.utilities.OrderUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    @Autowired
    private UserClient userClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private HeaderGenerator headerGenerator;

    // ==========================================
    // GET /order → lấy tất cả đơn hàng (admin)
    // ==========================================
    @GetMapping(value = "/order")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        if (orders != null && !orders.isEmpty()) {
            return new ResponseEntity<>(orders, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    // ==========================================
    // GET /order/{id}
    // ==========================================
    @GetMapping(value = "/order/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable("id") Long id) {
        Order order = orderService.getOrderById(id);
        if (order != null) {
            return new ResponseEntity<>(order, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    // ==========================================
    // GET /order/user/{userId}
    // ==========================================
    @GetMapping(value = "/order/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable("userId") Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        if (orders != null && !orders.isEmpty()) {
            return new ResponseEntity<>(orders, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    // ==========================================
    // POST /order/{userId} → đặt hàng qua Cookie (giữ nguyên cho tương thích)
    // ==========================================
    @PostMapping(value = "/order/{userId}")
    public ResponseEntity<Order> saveOrder(
            @PathVariable("userId") Long userId,
            @RequestHeader(value = "Cookie", required = false) String cartId,
            HttpServletRequest request) {

        if (cartId == null || cartId.isBlank()) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }

        List<Item> cart = cartService.getAllItemsFromCart(cartId);
        User user = userClient.getUserById(userId);

        if (cart != null && !cart.isEmpty() && user != null) {
            Order order = createOrder(cart, user);
            try {
                orderService.saveOrder(order);
                cartService.deleteCart(cartId);
                return new ResponseEntity<>(
                        order,
                        headerGenerator.getHeadersForSuccessPostMethod(request, order.getId()),
                        HttpStatus.CREATED);
            } catch (Exception ex) {
                ex.printStackTrace();
                return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    // ==========================================
    // POST /order/{userId}/direct → đặt hàng từ FE gửi cart trong body
    // FIX: Browser chặn set header "Cookie" → dùng endpoint này thay thế
    //
    // Body: [
    // { "productId": 1, "productName": "Cà phê", "price": 35000, "quantity": 2 },
    // ...
    // ]
    // ==========================================
    @PostMapping(value = "/order/{userId}/direct")
    public ResponseEntity<Order> saveOrderDirect(
            @PathVariable("userId") Long userId,
            @RequestBody List<Map<String, Object>> cartItems,
            HttpServletRequest request) {

        if (cartItems == null || cartItems.isEmpty()) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }

        User user = userClient.getUserById(userId);
        if (user == null) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        }

        try {
            // Convert cart items từ FE sang List<Item>
            List<Item> items = new ArrayList<>();
            for (Map<String, Object> cartItem : cartItems) {
                Product product = new Product();

                // productId
                Object pidObj = cartItem.get("productId");
                if (pidObj != null) {
                    product.setId(Long.parseLong(pidObj.toString()));
                }

                // productName
                Object nameObj = cartItem.get("productName");
                product.setProductName(nameObj != null ? nameObj.toString() : "");

                // price
                Object priceObj = cartItem.get("price");
                BigDecimal price = priceObj != null
                        ? new BigDecimal(priceObj.toString())
                        : BigDecimal.ZERO;
                product.setPrice(price);

                // quantity
                Object qtyObj = cartItem.get("quantity");
                int qty = qtyObj != null ? Integer.parseInt(qtyObj.toString()) : 1;

                BigDecimal subTotal = price.multiply(BigDecimal.valueOf(qty));

                Item item = new Item(qty, product, subTotal);
                items.add(item);
            }

            Order order = createOrder(items, user);
            orderService.saveOrder(order);

            return new ResponseEntity<>(
                    order,
                    headerGenerator.getHeadersForSuccessPostMethod(request, order.getId()),
                    HttpStatus.CREATED);

        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // PUT /order/{id}/status
    // ==========================================
    @PutMapping(value = "/order/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }
        try {
            Order updated = orderService.updateOrderStatus(id, status.toUpperCase());
            return new ResponseEntity<>(updated, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // PUT /order/{id}/payment-status
    // Được gọi nội bộ bởi Payment Service (Feign Client)
    // Param: status (PAID | PAYMENT_FAILED | REFUNDED)
    // ==========================================
    @PutMapping(value = "/order/{id}/payment-status")
    public ResponseEntity<Order> updatePaymentStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status) {
        try {
            Order updated = orderService.updateOrderStatus(id, status.toUpperCase());
            return new ResponseEntity<>(updated, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // DELETE /order/{id}
    // ==========================================
    @DeleteMapping(value = "/order/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) {
        try {
            orderService.deleteOrder(id);
            return new ResponseEntity<>(headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // Helper
    // ==========================================
    private Order createOrder(List<Item> cart, User user) {
        Order order = new Order();
        order.setItems(cart);
        order.setUser(user);
        order.setTotal(OrderUtilities.countTotalPrice(cart));
        order.setOrderedDate(LocalDate.now());
        order.setStatus("PAYMENT_EXPECTED");
        return order;
    }
}