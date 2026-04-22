package com.rainbowforest.productcatalogservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình serve static files (ảnh upload).
 *
 * Ảnh lưu tại: ./uploads/products/
 * Truy cập qua: http://localhost:8810/images/products/{filename}
 *
 * NOTE: CORS được xử lý tập trung tại API Gateway (CorsConfig.java).
 * KHÔNG cấu hình CORS ở đây để tránh duplicate header.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * Cho phép truy cập ảnh đã upload qua URL /images/**
     * VD: GET http://localhost:8810/images/products/product_1_abc123.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = "file:" + uploadDir.replace("\\", "/");
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        registry.addResourceHandler("/images/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600);
    }

    // ❌ KHÔNG override addCorsMappings() — CORS do API Gateway xử lý
}
