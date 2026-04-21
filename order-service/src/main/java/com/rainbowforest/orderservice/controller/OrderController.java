package com.rainbowforest.orderservice.controller;

import com.rainbowforest.orderservice.domain.DiningTable;
import com.rainbowforest.orderservice.domain.Item;
import com.rainbowforest.orderservice.domain.Order;
import com.rainbowforest.orderservice.domain.Product;
import com.rainbowforest.orderservice.domain.User;
import com.rainbowforest.orderservice.feignclient.UserClient;
import com.rainbowforest.orderservice.http.header.HeaderGenerator;
import com.rainbowforest.orderservice.http.request.TableOrderRequest;
import com.rainbowforest.orderservice.repository.DiningTableRepository;
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
import java.util.LinkedHashMap;
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

    @Autowired
    private DiningTableRepository tableRepository;

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
    // POST /order/{userId} → đặt hàng qua Cookie
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
            List<Item> items = parseCartItems(cartItems);
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
    // POST /order/table → nhân viên tạo order cho bàn
    // Body: { "tableId": 3, "customerName": "Nguyễn Văn A",
    // "items": [{ "productId":1, "productName":"Cà phê", "price":35000,
    // "quantity":2 }] }
    // ==========================================
    @PostMapping(value = "/order/table")
    public ResponseEntity<Order> saveTableOrder(
            @RequestBody TableOrderRequest tableRequest,
            HttpServletRequest httpRequest) {

        if (tableRequest.getTableId() == null
                || tableRequest.getItems() == null
                || tableRequest.getItems().isEmpty()) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }

        DiningTable table = tableRepository.findById(tableRequest.getTableId()).orElse(null);
        if (table == null) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        }

        try {
            List<Item> items = parseCartItems(tableRequest.getItems());

            Order order = new Order();
            order.setItems(items);
            order.setDiningTable(table);
            order.setCustomerName(tableRequest.getCustomerName());
            order.setTotal(OrderUtilities.countTotalPrice(items));
            order.setOrderedDate(LocalDate.now());
            order.setStatus("PAYMENT_EXPECTED");

            orderService.saveOrder(order);

            // Đánh dấu bàn đang bận
            table.setStatus("OCCUPIED");
            tableRepository.save(table);

            return new ResponseEntity<>(
                    order,
                    headerGenerator.getHeadersForSuccessPostMethod(httpRequest, order.getId()),
                    HttpStatus.CREATED);

        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // POST /order/{id}/checkout
    // Thanh toán xong → trả bàn về FREE, trả bill để in
    // ==========================================
    @PostMapping(value = "/order/{id}/checkout")
    public ResponseEntity<Map<String, Object>> checkoutOrder(@PathVariable("id") Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        }

        try {
            orderService.updateOrderStatus(id, "PAID");

            DiningTable table = order.getDiningTable();
            if (table != null) {
                table.setStatus("FREE"); // khớp với frontend (FREE thay vì AVAILABLE)
                tableRepository.save(table);
            }

            // Tạo bill trả về để FE in
            Map<String, Object> bill = new LinkedHashMap<>();
            bill.put("orderId", order.getId());
            bill.put("tableNumber", table != null ? table.getNumber() : "N/A"); // getNumber() đúng với entity
            bill.put("customerName", order.getCustomerName() != null ? order.getCustomerName() : "Khách lẻ");
            bill.put("orderedDate", order.getOrderedDate().toString());

            List<Map<String, Object>> billItems = new ArrayList<>();
            for (Item item : order.getItems()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productName", item.getProduct().getProductName());
                row.put("quantity", item.getQuantity());
                row.put("price", item.getProduct().getPrice());
                row.put("subtotal", item.getSubTotal());
                billItems.add(row);
            }
            bill.put("items", billItems);
            bill.put("total", order.getTotal());
            bill.put("status", "PAID");

            return new ResponseEntity<>(bill, HttpStatus.OK);

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
    // Gọi nội bộ bởi Payment Service
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
    // Helpers
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

    private List<Item> parseCartItems(List<Map<String, Object>> cartItems) {
        List<Item> items = new ArrayList<>();
        for (Map<String, Object> cartItem : cartItems) {
            Product product = new Product();

            Object pidObj = cartItem.get("productId");
            if (pidObj != null)
                product.setId(Long.parseLong(pidObj.toString()));

            Object nameObj = cartItem.get("productName");
            product.setProductName(nameObj != null ? nameObj.toString() : "");

            Object priceObj = cartItem.get("price");
            BigDecimal price = priceObj != null ? new BigDecimal(priceObj.toString()) : BigDecimal.ZERO;
            product.setPrice(price);

            Object qtyObj = cartItem.get("quantity");
            int qty = qtyObj != null ? Integer.parseInt(qtyObj.toString()) : 1;

            items.add(new Item(qty, product, price.multiply(BigDecimal.valueOf(qty))));
        }
        return items;
    }
}