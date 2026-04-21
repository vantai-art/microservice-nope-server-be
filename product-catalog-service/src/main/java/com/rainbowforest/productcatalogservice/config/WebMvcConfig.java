package com.rainbowforest.productcatalogservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình serve static files (ảnh upload) và CORS.
 *
 * Ảnh lưu tại: ./uploads/products/
 * Truy cập qua: http://localhost:8810/images/products/{filename}
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
        // Đảm bảo path kết thúc bằng /
        String resourceLocation = "file:" + uploadDir.replace("\\", "/");
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        registry.addResourceHandler("/images/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600); // cache 1 giờ
    }

    /**
     * CORS cho product-catalog-service
     * (Gateway ở port 8080 cũng cần cấu hình CORS cho /admin/products/{id}/image)
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
