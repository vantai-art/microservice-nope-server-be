package com.rainbowforest.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Service gửi email qua Gmail SMTP.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Gửi email chứa link đặt lại mật khẩu.
     *
     * @param toEmail   địa chỉ email người nhận
     * @param firstName tên người dùng để cá nhân hóa email
     * @param token     token reset password
     */
    public void sendPasswordResetEmail(String toEmail, String firstName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Foodie Hub 🍽️");
            helper.setTo(toEmail);
            helper.setSubject("Đặt lại mật khẩu - Foodie Hub");

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String htmlContent = buildEmailHtml(firstName, resetLink);

            helper.setText(htmlContent, true); // true = HTML
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage(), e);
        }
    }

    private String buildEmailHtml(String firstName, String resetLink) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0;padding:0;font-family:Arial,sans-serif;background:#f5f5f5;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f5f5f5;padding:40px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='560' cellpadding='0' cellspacing='0' style='background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);'>" +

            // Header
            "<tr><td style='background:linear-gradient(135deg,#e67e22,#d35400);padding:36px 40px;text-align:center;'>" +
            "<div style='font-size:42px;margin-bottom:8px;'>🍽️</div>" +
            "<h1 style='color:#fff;margin:0;font-size:26px;font-weight:800;letter-spacing:-0.5px;'>Foodie Hub</h1>" +
            "<p style='color:rgba(255,255,255,0.7);margin:6px 0 0;font-size:14px;'>Tinh tế từng bữa ăn</p>" +
            "</td></tr>" +

            // Body
            "<tr><td style='padding:40px;'>" +
            "<h2 style='color:#1a1a1a;margin:0 0 16px;font-size:22px;'>Xin chào, " + firstName + "! 👋</h2>" +
            "<p style='color:#555;line-height:1.7;margin:0 0 20px;'>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại <strong>Foodie Hub</strong>.</p>" +
            "<p style='color:#555;line-height:1.7;margin:0 0 32px;'>Nhấp vào nút bên dưới để tạo mật khẩu mới. Liên kết này sẽ hết hạn sau <strong>15 phút</strong>.</p>" +

            // Button
            "<div style='text-align:center;margin:0 0 32px;'>" +
            "<a href='" + resetLink + "' style='display:inline-block;background:linear-gradient(135deg,#e67e22,#d35400);color:#fff;text-decoration:none;padding:14px 36px;border-radius:12px;font-size:16px;font-weight:700;box-shadow:0 4px 16px rgba(230,126,34,0.4);'>🔐 Đặt lại mật khẩu</a>" +
            "</div>" +

            // Warning
            "<div style='background:#fff8f0;border:1px solid #fdd8a8;border-left:4px solid #e67e22;border-radius:10px;padding:16px 20px;margin-bottom:24px;'>" +
            "<p style='color:#c0651a;margin:0;font-size:13px;line-height:1.6;'>" +
            "<strong>⚠️ Lưu ý bảo mật:</strong><br>" +
            "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này. " +
            "Tài khoản của bạn vẫn an toàn và không có thay đổi nào được thực hiện." +
            "</p></div>" +

            // Link fallback
            "<p style='color:#999;font-size:12px;line-height:1.6;'>Nếu nút không hoạt động, hãy sao chép và dán liên kết sau vào trình duyệt:<br>" +
            "<span style='color:#e67e22;word-break:break-all;'>" + resetLink + "</span></p>" +
            "</td></tr>" +

            // Footer
            "<tr><td style='background:#f9f9f9;padding:24px 40px;border-top:1px solid #eee;text-align:center;'>" +
            "<p style='color:#bbb;font-size:12px;margin:0;'>© 2025 Foodie Hub · Email này được gửi tự động, vui lòng không trả lời.</p>" +
            "</td></tr>" +

            "</table>" +
            "</td></tr></table>" +
            "</body></html>";
    }
}
