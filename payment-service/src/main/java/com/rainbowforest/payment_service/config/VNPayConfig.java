package com.rainbowforest.payment_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.TimeZone;

@Component
public class VNPayConfig {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.frontend-success-url}")
    private String frontendSuccessUrl;

    @Value("${vnpay.frontend-fail-url}")
    private String frontendFailUrl;

    public String getTmnCode() {
        return tmnCode;
    }

    public String getHashSecret() {
        return hashSecret;
    }

    public String getVnpayUrl() {
        return vnpayUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getFrontendSuccessUrl() {
        return frontendSuccessUrl;
    }

    public String getFrontendFailUrl() {
        return frontendFailUrl;
    }

    /**
     * Tạo URL thanh toán VNPay.
     *
     * @param orderId       ID đơn hàng nội bộ
     * @param amount        Số tiền (VND, không có phần thập phân)
     * @param orderInfo     Mô tả đơn hàng (VD: "Thanh toan don hang #5")
     * @param clientIp      IP người dùng
     * @param transactionId mã giao dịch nội bộ (lưu vào vnp_TxnRef)
     */
    public String buildPaymentUrl(Long orderId, long amount, String orderInfo,
            String clientIp, String transactionId) {
        Map<String, String> params = new TreeMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        // VNPay nhân thêm 100 (35000đ → "3500000")
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", transactionId);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", clientIp);
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        params.put("vnp_CreateDate", createDate);
        // Thêm thời hạn thanh toán: 15 phút kể từ khi tạo
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        cal.add(Calendar.MINUTE, 15);
        params.put("vnp_ExpireDate", new SimpleDateFormat("yyyyMMddHHmmss").format(cal.getTime()));

        // Build query string
        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String encodedKey = URLEncoder.encode(e.getKey(), StandardCharsets.US_ASCII);
            String encodedValue = URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII);
            if (query.length() > 0) {
                query.append("&");
                hashData.append("&");
            }
            query.append(encodedKey).append("=").append(encodedValue);
            hashData.append(e.getKey()).append("=").append(encodedValue);
        }

        String secureHash = hmacSHA512(hashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return vnpayUrl + "?" + query;
    }

    /**
     * Xác minh chữ ký VNPay trả về.
     * Lấy tất cả params (trừ vnp_SecureHash), sort, HMAC-SHA512 rồi so sánh.
     */
    public boolean verifyReturnSignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null)
            return false;

        Map<String, String> filtered = new TreeMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!e.getKey().equals("vnp_SecureHash") && !e.getKey().equals("vnp_SecureHashType")) {
                filtered.put(e.getKey(), e.getValue());
            }
        }

        // ⚠️ QUAN TRỌNG: VNPay gửi callback với giá trị RAW (không encode).
        // Khi build chuỗi hashData để verify, phải dùng giá trị GỐC — KHÔNG encode.
        // Encode ở đây sẽ khiến chữ ký tính ra khác VNPay → mọi giao dịch đều "sai chữ
        // ký".
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> e : filtered.entrySet()) {
            if (hashData.length() > 0)
                hashData.append("&");
            hashData.append(e.getKey()).append("=").append(e.getValue());
        }

        String computedHash = hmacSHA512(hashSecret, hashData.toString());
        return computedHash.equalsIgnoreCase(receivedHash);
    }

    // ── HMAC-SHA512 ──────────────────────────────────────────────
    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo HMAC-SHA512", e);
        }
    }
}