package com.rainbowforest.payment_service.controller;

import com.rainbowforest.payment_service.config.VNPayConfig;
import com.rainbowforest.payment_service.dto.PaymentDto.*;
import com.rainbowforest.payment_service.model.Payment;
import com.rainbowforest.payment_service.service.payment_service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final payment_service paymentService;
    private final VNPayConfig vnPayConfig;

    // ──────────────────────────────────────────────────────────────
    // VNPAY: TẠO URL THANH TOÁN
    // POST /api/payments/vnpay/create
    // Body: { "orderId":1, "userId":1, "amount":150000 }
    // Trả về: { "paymentUrl": "https://sandbox.vnpayment.vn/..." }
    // ──────────────────────────────────────────────────────────────
    @PostMapping("/vnpay/create")
    public ResponseEntity<ApiResponse<Map<String, String>>> createVNPayUrl(
            @RequestBody CreateVNPayRequest req,
            HttpServletRequest request) {

        log.info("Tạo URL VNPay: orderId={}, amount={}", req.getOrderId(), req.getAmount());

        // 1. Lưu bản ghi payment với status PENDING
        CreatePaymentRequest createReq = new CreatePaymentRequest(
                req.getOrderId(),
                req.getUserId(),
                req.getAmount(),
                Payment.PaymentMethod.VNPAY);
        PaymentResponse payment = paymentService.createPayment(createReq);

        // 2. Lấy IP người dùng (qua gateway/proxy)
        String clientIp = getClientIp(request);

        // 3. Build URL VNPay — dùng transactionId làm vnp_TxnRef
        String orderInfo = "Thanh toan don hang #" + req.getOrderId();
        String paymentUrl = vnPayConfig.buildPaymentUrl(
                req.getOrderId(),
                req.getAmount().longValue(),
                orderInfo,
                clientIp,
                payment.getTransactionId());

        Map<String, String> result = new HashMap<>();
        result.put("paymentUrl", paymentUrl);
        result.put("transactionId", payment.getTransactionId());
        result.put("paymentId", payment.getId().toString());

        return ResponseEntity.ok(ApiResponse.success("URL thanh toán đã được tạo", result));
    }

    // ──────────────────────────────────────────────────────────────
    // VNPAY: NHẬN CALLBACK (IPN / Return URL)
    // GET /api/payments/vnpay/return?vnp_ResponseCode=00&vnp_TxnRef=...&...
    // VNPay redirect user về đây sau khi thanh toán
    // → Xác minh chữ ký → Cập nhật DB → Redirect FE
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/vnpay/return")
    public ResponseEntity<Void> vnpayReturn(@RequestParam Map<String, String> params) {
        log.info("VNPay callback: ResponseCode={}, TxnRef={}",
                params.get("vnp_ResponseCode"), params.get("vnp_TxnRef"));

        // 1. Xác minh chữ ký HMAC-SHA512
        if (!vnPayConfig.verifyReturnSignature(params)) {
            log.error("VNPay: Chữ ký không hợp lệ! params={}", params);
            // Redirect FE với lỗi
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(vnPayConfig.getFrontendFailUrl() + "?error=invalid_signature"))
                    .build();
        }

        String responseCode = params.get("vnp_ResponseCode"); // "00" = thành công
        String txnRef = params.get("vnp_TxnRef"); // transactionId nội bộ
        String vnpTransNo = params.get("vnp_TransactionNo"); // mã GD phía VNPay

        try {
            if ("00".equals(responseCode)) {
                // ── THANH TOÁN THÀNH CÔNG ──────────────────────────
                paymentService.confirmVNPaySuccess(txnRef, vnpTransNo);
                log.info("VNPay SUCCESS: txnRef={}", txnRef);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(vnPayConfig.getFrontendSuccessUrl()
                                + "?vnpay=success&txnRef=" + txnRef))
                        .build();
            } else {
                // ── THANH TOÁN THẤT BẠI / HỦY ─────────────────────
                String reason = mapResponseCode(responseCode);
                paymentService.confirmVNPayFailed(txnRef, reason);
                log.warn("VNPay FAILED: txnRef={}, code={}", txnRef, responseCode);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(vnPayConfig.getFrontendFailUrl()
                                + "?vnpay=failed&code=" + responseCode))
                        .build();
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý callback VNPay: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(vnPayConfig.getFrontendFailUrl() + "?error=server_error"))
                    .build();
        }
    }

    // ──────────────────────────────────────────────────────────────
    // VNPAY: IPN (Instant Payment Notification) — server-to-server
    // GET /api/payments/vnpay/ipn
    // VNPay gọi ngầm server này để xác nhận (không qua browser)
    // Phải trả về {"RspCode":"00","Message":"Confirm Success"}
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(
            @RequestParam Map<String, String> params) {

        log.info("VNPay IPN: TxnRef={}, ResponseCode={}",
                params.get("vnp_TxnRef"), params.get("vnp_ResponseCode"));

        Map<String, String> resp = new HashMap<>();

        if (!vnPayConfig.verifyReturnSignature(params)) {
            resp.put("RspCode", "97");
            resp.put("Message", "Invalid Checksum");
            return ResponseEntity.ok(resp);
        }

        try {
            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            String vnpTransNo = params.get("vnp_TransactionNo");

            if ("00".equals(responseCode)) {
                paymentService.confirmVNPaySuccess(txnRef, vnpTransNo);
            } else {
                paymentService.confirmVNPayFailed(txnRef, mapResponseCode(responseCode));
            }

            resp.put("RspCode", "00");
            resp.put("Message", "Confirm Success");
        } catch (Exception e) {
            log.error("IPN error: {}", e.getMessage());
            resp.put("RspCode", "99");
            resp.put("Message", "Unknown error");
        }
        return ResponseEntity.ok(resp);
    }

    // ──────────────────────────────────────────────────────────────
    // ENDPOINTS CŨ GIỮ NGUYÊN
    // ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> create(
            @Valid @RequestBody CreatePaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo thanh toán thành công", paymentService.createPayment(req)));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> process(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Xử lý thanh toán hoàn tất", paymentService.processPayment(id)));
    }

    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(
            @Valid @RequestBody RefundRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success("Hoàn tiền thành công", paymentService.refundPayment(req)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PaymentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật trạng thái thành công", paymentService.updateStatus(id, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getById(id)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByOrderId(orderId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByUserId(userId)));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByTxnId(@PathVariable String transactionId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByTransactionId(transactionId)));
    }

    // ──────────────────────────────────────────────────────────────
    // HELPERS
    // ──────────────────────────────────────────────────────────────

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Lấy IP đầu tiên nếu có nhiều
        if (ip != null && ip.contains(","))
            ip = ip.split(",")[0].trim();
        return ip != null ? ip : "127.0.0.1";
    }

    private String mapResponseCode(String code) {
        return switch (code) {
            case "07" -> "Giao dịch bị nghi ngờ gian lận";
            case "09" -> "Thẻ/Tài khoản chưa đăng ký InternetBanking";
            case "10" -> "Xác thực thông tin thẻ/tài khoản quá 3 lần";
            case "11" -> "Đã hết hạn chờ thanh toán";
            case "12" -> "Thẻ/Tài khoản bị khóa";
            case "13" -> "Sai OTP";
            case "24" -> "Khách hàng hủy giao dịch";
            case "51" -> "Tài khoản không đủ số dư";
            case "65" -> "Tài khoản vượt quá hạn mức giao dịch trong ngày";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            case "79" -> "Sai mật khẩu thanh toán quá số lần quy định";
            default -> "Giao dịch thất bại (mã: " + code + ")";
        };
    }

    // ──────────────────────────────────────────────────────────────
    // DTO nội bộ cho VNPay
    // ──────────────────────────────────────────────────────────────
    @lombok.Data
    public static class CreateVNPayRequest {
        private Long orderId;
        private Long userId;
        private BigDecimal amount;
    }
}