package com.rainbowforest.userservice.service;

import com.rainbowforest.userservice.entity.PasswordResetToken;
import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.repository.PasswordResetTokenRepository;
import com.rainbowforest.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý luồng quên mật khẩu:
 * 1. Tìm user theo email
 * 2. Tạo token và lưu DB
 * 3. Gửi email chứa link reset
 * 4. Xác thực token khi user bấm link
 * 5. Cập nhật mật khẩu mới
 */
@Service
@Transactional
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.reset.token.expiry.minutes:15}")
    private int tokenExpiryMinutes;

    /**
     * Bước 1: Người dùng nhập email → gửi email reset.
     * Trả về true dù email có tồn tại hay không (tránh lộ thông tin).
     */
    public void requestPasswordReset(String email) {
        // Tìm user có email này trong bảng users_details
        List<User> allUsers = userRepository.findAll();
        User foundUser = allUsers.stream()
                .filter(u -> u.getUserDetails() != null
                        && email.equalsIgnoreCase(u.getUserDetails().getEmail()))
                .findFirst()
                .orElse(null);

        // Nếu không tìm thấy email → không làm gì (bảo mật: không thông báo lỗi)
        if (foundUser == null) return;

        // Xóa token cũ của user này (nếu có)
        tokenRepository.deleteByUserId(foundUser.getId());

        // Tạo token ngẫu nhiên
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);

        PasswordResetToken resetToken = new PasswordResetToken(token, foundUser, expiresAt);
        tokenRepository.save(resetToken);

        // Gửi email
        String firstName = foundUser.getUserDetails().getFirstName();
        emailService.sendPasswordResetEmail(email, firstName, token);
    }

    /**
     * Bước 2: Xác thực token (dùng khi user bấm link trong email).
     * @return thông tin user nếu token hợp lệ, null nếu không
     */
    public User validateResetToken(String token) {
        Optional<PasswordResetToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty()) return null;

        PasswordResetToken resetToken = opt.get();
        if (resetToken.isUsed() || resetToken.isExpired()) return null;

        return resetToken.getUser();
    }

    /**
     * Bước 3: Đặt mật khẩu mới sau khi token đã được xác thực.
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty()) return false;

        PasswordResetToken resetToken = opt.get();
        if (resetToken.isUsed() || resetToken.isExpired()) return false;

        // Cập nhật mật khẩu
        User user = resetToken.getUser();
        user.setUserPassword(newPassword);
        userRepository.save(user);

        // Đánh dấu token đã dùng
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return true;
    }
}
