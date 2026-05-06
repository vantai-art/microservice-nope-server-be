package com.rainbowforest.payment_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

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
     */
    public String buildPaymentUrl(Long orderId, long amount, String orderInfo,
            String clientIp, String transactionId) {
        Map<String, String> params = new TreeMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", transactionId);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", clientIp);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        params.put("vnp_CreateDate", sdf.format(cal.getTime()));
        cal.add(Calendar.MINUTE, 15);
        params.put("vnp_ExpireDate", sdf.format(cal.getTime()));

        // Build query string và hashData
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
            // hashData dùng key gốc + value đã encode (chuẩn VNPay)
            hashData.append(e.getKey()).append("=").append(encodedValue);
        }

        String secureHash = hmacSHA512(hashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return vnpayUrl + "?" + query;
    }

    /**
     * Xác minh chữ ký VNPay trả về.
     *
     * FIX: Spring MVC đã tự decode query params trước khi vào method.
     * VNPay tính hash trên giá trị URL-encoded → cần encode lại từng value
     * theo đúng chuẩn VNPay (UTF-8 encode, dùng %20 thay space).
     *
     * Quy trình đúng:
     * 1. Lọc bỏ vnp_SecureHash, vnp_SecureHashType
     * 2. Sort theo key alphabet (TreeMap)
     * 3. Encode từng value bằng UTF-8 (VNPay dùng UTF-8, không phải US-ASCII)
     * 4. Build hashData: key=encodedValue&...
     * 5. HMAC-SHA512 → so sánh với vnp_SecureHash nhận được
     */
    public boolean verifyReturnSignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null)
            return false;

        // Dùng TreeMap để sort key theo alphabet tự động
        Map<String, String> filtered = new TreeMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String key = e.getKey();
            if (!key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")) {
                filtered.put(key, e.getValue());
            }
        }

        // Build hashData: encode value bằng UTF-8 (chuẩn VNPay sandbox)
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> e : filtered.entrySet()) {
            if (hashData.length() > 0)
                hashData.append("&");
            String encodedValue = URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8);
            hashData.append(e.getKey()).append("=").append(encodedValue);
        }

        String computedHash = hmacSHA512(hashSecret, hashData.toString());
        boolean valid = computedHash.equalsIgnoreCase(receivedHash);

        if (!valid) {
            org.slf4j.LoggerFactory.getLogger(VNPayConfig.class)
                    .error("VNPay verify FAILED.\nHashData: {}\nComputed:  {}\nReceived:  {}",
                            hashData, computedHash, receivedHash);
        } else {
            org.slf4j.LoggerFactory.getLogger(VNPayConfig.class)
                    .info("VNPay verify OK: txnRef={}", params.get("vnp_TxnRef"));
        }

        return valid;
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