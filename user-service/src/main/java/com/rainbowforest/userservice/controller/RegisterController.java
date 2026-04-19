package com.rainbowforest.userservice.controller;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.http.header.HeaderGenerator;
import com.rainbowforest.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private HeaderGenerator headerGenerator;

    @PostMapping(value = "/registration")
    public ResponseEntity<?> addUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Dữ liệu không hợp lệ"));
        }

        // Validate các trường bắt buộc
        if (user.getUserName() == null || user.getUserName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Tên đăng nhập không được để trống"));
        }
        if (user.getUserPassword() == null || user.getUserPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Mật khẩu không được để trống"));
        }
        if (user.getUserDetails() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Thông tin chi tiết người dùng không được để trống"));
        }
        if (user.getUserDetails().getEmail() == null || user.getUserDetails().getEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email không được để trống"));
        }
        if (user.getUserDetails().getFirstName() == null || user.getUserDetails().getFirstName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Họ không được để trống"));
        }
        if (user.getUserDetails().getLastName() == null || user.getUserDetails().getLastName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Tên không được để trống"));
        }

        try {
            userService.saveUser(user);
            return new ResponseEntity<>(
                    user,
                    headerGenerator.getHeadersForSuccessPostMethod(request, user.getId()),
                    HttpStatus.CREATED);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Lỗi duplicate: userName hoặc email đã tồn tại
            String msg = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
            if (msg != null && msg.contains("user_name")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Tên đăng nhập '" + user.getUserName() + "' đã được sử dụng"));
            }
            if (msg != null && msg.contains("email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Email '" + user.getUserDetails().getEmail() + "' đã được sử dụng"));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Tên đăng nhập hoặc email đã tồn tại"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server: " + e.getMessage()));
        }
    }
}