package com.rainbowforest.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * ========================================
 * CORS Configuration cho API Gateway
 * ========================================
 *
 * Lý do cần file này:
 * - application.properties dùng allowed-origins=* (wildcard)
 * - Khi frontend gửi credentials: 'include' (cookie/session),
 * browser yêu cầu origin phải được chỉ định rõ ràng, KHÔNG được dùng *
 * - File này override config trong properties, cho phép cụ thể từng origin
 *
 * Thêm origin mới: bổ sung vào danh sách ALLOWED_ORIGINS bên dưới
 */
@Configuration
public class CorsConfig {

    // ✅ Danh sách origin được phép — thêm origin mới vào đây
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:8081", // Expo Web / React Native Web (dev)
            "http://localhost:3000", // React Web (nếu có)
            "http://localhost:19006", // Expo Web alt port
            "http://10.0.2.2:8081", // Android Emulator → host machine
            "http://127.0.0.1:8081" // Loopback alias
    );

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ Chỉ định origin cụ thể (bắt buộc khi dùng credentials)
        config.setAllowedOrigins(ALLOWED_ORIGINS);

        // ✅ Cho phép tất cả HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // ✅ Cho phép tất cả headers
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));

        // ✅ Cho phép gửi cookie/session (bắt buộc để session hoạt động)
        config.setAllowCredentials(true);

        // Cache preflight OPTIONS request trong 1 giờ
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}