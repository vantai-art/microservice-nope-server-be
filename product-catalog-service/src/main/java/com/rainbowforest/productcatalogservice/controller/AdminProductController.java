package com.rainbowforest.productcatalogservice.controller;

import com.rainbowforest.productcatalogservice.entity.Product;
import com.rainbowforest.productcatalogservice.http.header.HeaderGenerator;
import com.rainbowforest.productcatalogservice.service.ActivityLogService;
import com.rainbowforest.productcatalogservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private HeaderGenerator headerGenerator;

    @Autowired
    private ActivityLogService activityLogService;

    // POST /admin/products → thêm sản phẩm mới
    // Header: X-Performed-By (tên người dùng), X-Role (ADMIN hoặc STAFF)
    @PostMapping(value = "/products")
    public ResponseEntity<Product> addProduct(
            @RequestBody Product product,
            HttpServletRequest request) {

        if (product != null) {
            try {
                Product saved = productService.addProduct(product);

                // Ghi log
                String performedBy = getPerformedBy(request);
                String role = getRole(request);
                activityLogService.logAdd(performedBy, role, saved);

                return new ResponseEntity<>(
                        saved,
                        headerGenerator.getHeadersForSuccessPostMethod(request, saved.getId()),
                        HttpStatus.CREATED);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
    }

    // PUT /admin/products/{id} → cập nhật sản phẩm
    @PutMapping(value = "/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable("id") Long id,
            @RequestBody Product product,
            HttpServletRequest request) {
        try {
            // Lấy dữ liệu cũ trước khi update để ghi log
            Product before = productService.getProductById(id);
            // Clone trước khi update (vì JPA sẽ thay đổi object trong session)
            Product beforeSnapshot = cloneProduct(before);

            Product updated = productService.updateProduct(id, product);

            // Ghi log
            String performedBy = getPerformedBy(request);
            String role = getRole(request);
            activityLogService.logUpdate(performedBy, role, beforeSnapshot, updated);

            return new ResponseEntity<>(updated, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // DELETE /admin/products/{id} → xóa sản phẩm
    @DeleteMapping(value = "/products/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") Long id,
            HttpServletRequest request) {
        try {
            // Lấy thông tin sản phẩm trước khi xóa để ghi log
            Product product = productService.getProductById(id);
            if (product == null) {
                return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
            }

            productService.deleteProduct(id);

            // Ghi log
            String performedBy = getPerformedBy(request);
            String role = getRole(request);
            activityLogService.logDelete(performedBy, role, product);

            return new ResponseEntity<>(headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    /**
     * Lấy tên người thực hiện từ header X-Performed-By.
     * FE cần gửi header này khi gọi API admin.
     * Ví dụ: X-Performed-By: nguyen.van.a
     */
    private String getPerformedBy(HttpServletRequest request) {
        String value = request.getHeader("X-Performed-By");
        return (value != null && !value.isBlank()) ? value : "unknown";
    }

    /**
     * Lấy role từ header X-Role.
     * FE cần gửi header này khi gọi API admin.
     * Ví dụ: X-Role: ADMIN hoặc X-Role: STAFF
     */
    private String getRole(HttpServletRequest request) {
        String value = request.getHeader("X-Role");
        return (value != null && !value.isBlank()) ? value.toUpperCase() : "UNKNOWN";
    }

    /**
     * Clone product để lưu snapshot trước khi update
     */
    private Product cloneProduct(Product p) {
        if (p == null)
            return null;
        Product clone = new Product();
        clone.setId(p.getId());
        clone.setProductName(p.getProductName());
        clone.setPrice(p.getPrice());
        clone.setDiscription(p.getDiscription());
        clone.setCategory(p.getCategory());
        clone.setAvailability(p.getAvailability());
        return clone;
    }
}