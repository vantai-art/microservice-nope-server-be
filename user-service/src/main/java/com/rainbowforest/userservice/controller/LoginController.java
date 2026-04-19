package com.rainbowforest.userservice.controller;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String userName = body.get("userName");
        String userPassword = body.get("userPassword");

        // Validate input
        if (userName == null || userPassword == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Vui lòng nhập tên đăng nhập và mật khẩu"));
        }

        // Tìm user theo username
        User user = userRepository.findByUserName(userName);

        // Kiểm tra user tồn tại và password đúng
        if (user == null || !user.getUserPassword().equals(userPassword)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sai tên đăng nhập hoặc mật khẩu"));
        }

        // Kiểm tra tài khoản có bị khóa không
        if (user.getActive() == 0) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Tài khoản đã bị khóa. Vui lòng liên hệ admin."));
        }

        // Trả về thông tin cần thiết — KHÔNG trả password
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("userName", user.getUserName());
        result.put("active", user.getActive());
        result.put("userDetails", user.getUserDetails());
        result.put("role", user.getRole() != null ? user.getRole().getRoleName() : "ROLE_USER");

        return ResponseEntity.ok(result);
    }
}