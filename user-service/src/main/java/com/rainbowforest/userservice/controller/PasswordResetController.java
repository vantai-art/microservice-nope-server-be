package com.rainbowforest.userservice.controller;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API quên mật khẩu / đặt lại mật khẩu.
 *
 * POST /auth/forgot-password → nhận email, gửi link về Gmail
 * GET /auth/validate-token → kiểm tra token còn hợp lệ không
 * POST /auth/reset-password → đặt mật khẩu mới bằng token
 *
 * FIX: Bỏ @CrossOrigin("*") — CORS đã được xử lý tập trung tại API Gateway
 * (CorsWebFilter).
 * Để @CrossOrigin("*") trên controller khi gateway dùng withCredentials sẽ gây
 * conflict
 * và browser block request hoàn toàn (lỗi undefined, không có HTTP response).
 */
@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * Bước 1: Người dùng nhập email → backend tìm user và gửi email reset.
     * Luôn trả về 200 OK dù email có tồn tại hay không (bảo mật).
     *
     * Request body: { "email": "user@gmail.com" }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Vui lòng nhập địa chỉ email"));
        }

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Địa chỉ email không hợp lệ"));
        }

        try {
            passwordResetService.requestPasswordReset(email.trim().toLowerCase());
        } catch (Exception e) {
            System.err.println("[PasswordReset] Error sending email: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "message", "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu."));
    }

    /**
     * Bước 2: Frontend gọi để kiểm tra token còn hợp lệ không.
     *
     * Query param: ?token=abc123...
     */
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam("token") String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "message", "Token không hợp lệ"));
        }

        User user = passwordResetService.validateResetToken(token);
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "message", "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"));
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "firstName", user.getUserDetails() != null ? user.getUserDetails().getFirstName() : "bạn"));
    }

    /**
     * Bước 3: Người dùng nhập mật khẩu mới và submit.
     *
     * Request body: { "token": "abc123...", "newPassword": "newpass123" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Token không hợp lệ"));
        }
        if (newPassword == null || newPassword.length() < 4) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Mật khẩu mới phải có ít nhất 4 ký tự"));
        }

        boolean success = passwordResetService.resetPassword(token, newPassword);
        if (!success) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Liên kết đã hết hạn hoặc đã được sử dụng. Vui lòng yêu cầu lại."));
        }

        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập ngay bây giờ."));
    }
}