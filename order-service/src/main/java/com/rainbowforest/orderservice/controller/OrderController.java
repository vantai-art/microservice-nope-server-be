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
import com.rainbowforest.orderservice.repository.ProductRepository;
import com.rainbowforest.orderservice.service.CartService;
import com.rainbowforest.orderservice.service.OrderService;
import com.rainbowforest.orderservice.utilities.OrderUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

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

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SimpMessagingTemplate ws;

    // ==========================================
    // GET /order
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
    // GET /order/table/{tableId} → lấy order đang mở của bàn
    // ==========================================
    @GetMapping(value = "/order/table/{tableId}")
    public ResponseEntity<List<Order>> getOrdersByTable(@PathVariable("tableId") Long tableId) {
        List<Order> orders = orderService.getAllOrders().stream()
                .filter(o -> o.getDiningTable() != null
                        && tableId.equals(o.getDiningTable().getId())
                        && !"PAID".equals(o.getStatus())
                        && !"CANCELLED".equals(o.getStatus()))
                .toList();
        if (orders.isEmpty()) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(orders, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
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
                order = orderService.saveOrder(order);
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
    // POST /order/{userId}/direct
    // ==========================================
    @PostMapping(value = "/order/{userId}/direct")
    public ResponseEntity<Order> saveOrderDirect(
            @PathVariable("userId") Long userId,
            @RequestBody List<Map<String, Object>> cartItems,
            HttpServletRequest request) {

        if (cartItems == null || cartItems.isEmpty()) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setId(userId);

        try {
            User fetchedUser = userClient.getUserById(userId);
            if (fetchedUser != null && fetchedUser.getUserName() != null) {
                user.setUserName(fetchedUser.getUserName());
            }
        } catch (Exception ignored) {
        }

        try {
            log.info("Tạo đơn hàng direct cho userId={}, số sản phẩm={}", userId, cartItems.size());
            List<Item> items = parseCartItems(cartItems);
            Order order = createOrder(items, user);
            order = orderService.saveOrder(order);

            return new ResponseEntity<>(
                    order,
                    headerGenerator.getHeadersForSuccessPostMethod(request, order.getId()),
                    HttpStatus.CREATED);

        } catch (Exception ex) {
            log.error("Lỗi tạo đơn hàng direct userId={}: {}", userId, ex.getMessage(), ex);
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // POST /order/table → nhân viên / khách tạo order cho bàn
    // Body: { "tableId": 3, "customerName": "Nguyễn Văn A", "note": "...",
    // "items": [{ "productId":1, "productName":"Cà phê",
    // "price":35000, "quantity":2 }] }
    // ==========================================
    @PostMapping(value = "/order/table")
    public ResponseEntity<Order> saveTableOrder(
            @RequestBody TableOrderRequest tableRequest,
            HttpServletRequest httpRequest) {

        if (tableRequest.getItems() == null || tableRequest.getItems().isEmpty()) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }

        // Tìm bàn — ưu tiên tableId (Long), fallback tableKey (String)
        DiningTable table = null;
        if (tableRequest.getTableId() != null) {
            table = tableRepository.findById(tableRequest.getTableId()).orElse(null);
        }
        if (table == null && tableRequest.getTableKey() != null) {
            table = tableRepository.findByTableKey(tableRequest.getTableKey()).orElse(null);
        }
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
            if (tableRequest.getNote() != null) {
                // note lưu vào customerName tạm, hoặc bạn thêm field note vào Order
                log.info("Ghi chú bàn {}: {}", table.getNumber(), tableRequest.getNote());
            }

            order = orderService.saveOrder(order);

            // Đánh dấu bàn đang bận
            table.setStatus("OCCUPIED");
            tableRepository.save(table);

            // ── Broadcast real-time cho staff dashboard ──
            ws.convertAndSend("/topic/table/" + table.getId(),
                    Map.of("type", "table:updated",
                            "tableId", table.getId(),
                            "data", order));

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
    // POST /order/{id}/checkout → thanh toán xong
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
                table.setStatus("FREE");
                tableRepository.save(table);

                // ── Broadcast thanh toán xong ──
                ws.convertAndSend("/topic/table/" + table.getId(),
                        Map.of("type", "table:paid",
                                "tableId", table.getId()));
            }

            // Tạo bill trả về để FE in
            Map<String, Object> bill = new LinkedHashMap<>();
            bill.put("orderId", order.getId());
            bill.put("tableNumber", table != null ? table.getNumber() : "N/A");
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
    // POST /order/{id}/request-checkout
    // Khách yêu cầu thanh toán → nhân viên nhận thông báo
    // ==========================================
    @PostMapping(value = "/order/{id}/request-checkout")
    public ResponseEntity<Void> requestCheckout(@PathVariable("id") Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        }
        try {
            orderService.updateOrderStatus(id, "WAITING_PAYMENT");
            DiningTable table = order.getDiningTable();
            if (table != null) {
                // Broadcast yêu cầu thanh toán cho staff
                ws.convertAndSend("/topic/table/" + table.getId(),
                        Map.of("type", "table:request_checkout",
                                "tableId", table.getId()));
            }
            return ResponseEntity.ok().build();
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
            Object pidObj = cartItem.get("productId");
            Long productId = null;
            try {
                if (pidObj != null)
                    productId = Long.parseLong(pidObj.toString());
            } catch (NumberFormatException e) {
                log.warn("productId không hợp lệ: {}", pidObj);
            }

            Object nameObj = cartItem.get("productName");
            String productName = (nameObj != null && !nameObj.toString().isBlank())
                    ? nameObj.toString().trim()
                    : (productId != null ? "San pham #" + productId : "San pham");

            BigDecimal price = BigDecimal.ZERO;
            try {
                Object priceObj = cartItem.get("price");
                if (priceObj != null) {
                    double d = Double.parseDouble(priceObj.toString());
                    price = Double.isFinite(d) ? BigDecimal.valueOf(d) : BigDecimal.ZERO;
                }
            } catch (NumberFormatException e) {
                log.warn("Gia san pham khong hop le: {}", cartItem.get("price"));
            }

            int qty = 1;
            try {
                Object qtyObj = cartItem.get("quantity");
                if (qtyObj == null)
                    qtyObj = cartItem.get("qty"); // fallback "qty"
                if (qtyObj != null)
                    qty = Math.max(1, Integer.parseInt(qtyObj.toString()));
            } catch (NumberFormatException e) {
                log.warn("Quantity khong hop le: {}", cartItem.get("quantity"));
            }

            Product product;
            if (productId != null) {
                final Long fid = productId;
                final String fname = productName;
                final BigDecimal fprice = price;
                product = productRepository.findByProductId(fid).orElseGet(() -> {
                    Product p = new Product();
                    p.setProductId(fid); // product_id = id từ catalog
                    p.setProductName(fname);
                    p.setPrice(fprice);
                    return productRepository.save(p);
                });
            } else {
                Product p = new Product();
                p.setProductName(productName);
                p.setPrice(price);
                product = productRepository.save(p);
            }

            log.info("  item: {} x{} @ {}", product.getProductName(), qty, price);
            items.add(new Item(qty, product, price.multiply(BigDecimal.valueOf(qty))));
        }
        return items;
    }
}