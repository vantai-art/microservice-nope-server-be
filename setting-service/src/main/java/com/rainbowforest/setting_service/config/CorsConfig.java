package com.rainbowforest.setting_service.config;

/**
 * CORS được xử lý tập trung tại API Gateway (CorsConfig.java).
 *
 * File này được giữ lại để tránh lỗi import nếu có reference,
 * nhưng KHÔNG đăng ký CorsFilter — tránh duplicate header
 * 'Access-Control-Allow-Origin' khi request đi qua Gateway.
 *
 * Nếu gọi setting-service trực tiếp (không qua Gateway),
 * hãy uncomment bean bên dưới và bỏ chú thích import.
 */
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;
// import java.util.List;
//
// @Configuration
// public class CorsConfig {
//     @Bean
//     public CorsFilter corsFilter() {
//         CorsConfiguration config = new CorsConfiguration();
//         config.setAllowedOriginPatterns(List.of("http://localhost:3000"));
//         config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//         config.setAllowedHeaders(List.of("*"));
//         config.setAllowCredentials(true);
//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", config);
//         return new CorsFilter(source);
//     }
// }
