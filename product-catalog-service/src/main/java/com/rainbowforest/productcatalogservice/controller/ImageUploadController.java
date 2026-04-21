package com.rainbowforest.productcatalogservice.controller;

import com.rainbowforest.productcatalogservice.entity.Product;
import com.rainbowforest.productcatalogservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller upload / xóa ảnh sản phẩm.
 *
 * POST   /admin/products/{id}/image   — upload ảnh mới (multipart file)
 * DELETE /admin/products/{id}/image   — xóa ảnh sản phẩm
 *
 * Ảnh được lưu tại: <upload-dir>/products/{filename}
 * URL trả về:       http://localhost:8810/images/products/{filename}
 */
@RestController
@RequestMapping("/admin/products")
@CrossOrigin(origins = "*")
public class ImageUploadController {

    @Autowired
    private ProductService productService;

    // Thư mục lưu ảnh — có thể override trong application.properties:
    //   app.upload-dir=./uploads
    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    // Base URL để tạo imageUrl trả về FE
    @Value("${app.base-url:http://localhost:8810}")
    private String baseUrl;

    // ─────────────────────────────────────────────────────────────
    // POST /admin/products/{id}/image
    // Content-Type: multipart/form-data
    // Param: file (MultipartFile)
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/{id}/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        Map<String, String> result = new HashMap<>();

        // Validate product tồn tại
        Product product = productService.getProductById(id);
        if (product == null) {
            result.put("error", "Không tìm thấy sản phẩm id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        // Validate file
        if (file == null || file.isEmpty()) {
            result.put("error", "File không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            result.put("error", "Chỉ chấp nhận file hình ảnh (jpg, png, webp, gif)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        // Giới hạn 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            result.put("error", "Kích thước file không được vượt quá 5MB");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        try {
            // Tạo thư mục nếu chưa có
            Path dirPath = Paths.get(uploadDir, "products");
            Files.createDirectories(dirPath);

            // Xóa ảnh cũ nếu có (chỉ xóa nếu lưu local)
            if (product.getImageUrl() != null && product.getImageUrl().contains("/images/products/")) {
                String oldFilename = product.getImageUrl().substring(product.getImageUrl().lastIndexOf('/') + 1);
                Path oldFile = dirPath.resolve(oldFilename);
                Files.deleteIfExists(oldFile);
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }
            String newFilename = "product_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            // Lưu file
            Path targetPath = dirPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Cập nhật imageUrl trong DB
            String imageUrl = baseUrl + "/images/products/" + newFilename;
            product.setImageUrl(imageUrl);
            productService.addProduct(product); // save

            result.put("imageUrl", imageUrl);
            result.put("message", "Upload ảnh thành công");
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            e.printStackTrace();
            result.put("error", "Lỗi khi lưu file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /admin/products/{id}/image
    // Xóa ảnh của sản phẩm (xóa file + set imageUrl = null)
    // ─────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}/image")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable Long id) {
        Map<String, String> result = new HashMap<>();

        Product product = productService.getProductById(id);
        if (product == null) {
            result.put("error", "Không tìm thấy sản phẩm id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        // Xóa file nếu lưu local
        if (product.getImageUrl() != null && product.getImageUrl().contains("/images/products/")) {
            String filename = product.getImageUrl().substring(product.getImageUrl().lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir, "products", filename);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Không cần báo lỗi nếu file không tồn tại
            }
        }

        // Xóa imageUrl trong DB
        product.setImageUrl(null);
        productService.addProduct(product);

        result.put("message", "Đã xóa ảnh sản phẩm");
        return ResponseEntity.ok(result);
    }
}
