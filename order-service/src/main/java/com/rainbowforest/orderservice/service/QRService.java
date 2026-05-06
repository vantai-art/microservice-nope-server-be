package com.rainbowforest.orderservice.service;

import com.rainbowforest.orderservice.domain.DiningTable;
import com.rainbowforest.orderservice.repository.DiningTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class QRService {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.qr.secret:rainbowforest-secret-key-32chars}")
    private String secret;

    @Autowired
    private DiningTableRepository tableRepository;

    /**
     * Tạo QR URL cho bàn theo id (Long)
     * Token = base64(tableId:expiry:hmac)
     */
    public String generateQRUrl(Long tableId) {
        long exp = System.currentTimeMillis() + Duration.ofDays(1).toMillis();
        String payload = tableId + ":" + exp;
        String sig = hmac(payload);
        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + ":" + sig).getBytes(StandardCharsets.UTF_8));

        // Lưu token vào DB
        tableRepository.findById(tableId).ifPresent(t -> {
            t.setQrToken(token);
            t.setQrExpiresAt(LocalDateTime.now().plusDays(1));
            tableRepository.save(t);
        });

        return frontendUrl + "/order?token=" + token;
    }

    /**
     * Giải mã token → trả về DiningTable
     */
    public DiningTable resolveTable(String token) {
        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length < 3)
                throw new IllegalArgumentException("Token không hợp lệ");

            Long tableId = Long.parseLong(parts[0]);
            long exp = Long.parseLong(parts[1]);
            String sig = parts[2];

            if (System.currentTimeMillis() > exp)
                throw new IllegalStateException("QR đã hết hạn");
            if (!hmac(tableId + ":" + exp).equals(sig))
                throw new SecurityException("QR không hợp lệ");

            return tableRepository.findById(tableId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Token lỗi: " + e.getMessage());
        }
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}