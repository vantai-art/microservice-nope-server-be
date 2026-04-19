package com.rainbowforest.payment_service.dto;

import com.rainbowforest.payment_service.model.Payment;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    // ---------- REQUEST ----------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreatePaymentRequest {

        @NotNull(message = "orderId không được để trống")
        private Long orderId;

        @NotNull(message = "userId không được để trống")
        private Long userId;

        @NotNull(message = "amount không được để trống")
        @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
        private BigDecimal amount;

        @NotNull(message = "method không được để trống")
        private Payment.PaymentMethod method;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {
        @NotNull(message = "status không được để trống")
        private Payment.PaymentStatus status;
        private String failureReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefundRequest {
        @NotNull(message = "orderId không được để trống")
        private Long orderId;
        private String reason;
    }

    // ---------- RESPONSE ----------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentResponse {
        private Long id;
        private Long orderId;
        private Long userId;
        private BigDecimal amount;
        private Payment.PaymentMethod method;
        private Payment.PaymentStatus status;
        private String transactionId;
        private String vnpayTransactionNo;
        private String failureReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static PaymentResponse from(Payment p) {
            return PaymentResponse.builder()
                    .id(p.getId())
                    .orderId(p.getOrderId())
                    .userId(p.getUserId())
                    .amount(p.getAmount())
                    .method(p.getMethod())
                    .status(p.getStatus())
                    .transactionId(p.getTransactionId())
                    .vnpayTransactionNo(p.getVnpayTransactionNo())
                    .failureReason(p.getFailureReason())
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .build();
        }
    }

    // ---------- API WRAPPER ----------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponse<T> {
        private int code;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder().code(200).message("Thành công").data(data).build();
        }

        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder().code(200).message(message).data(data).build();
        }

        public static <T> ApiResponse<T> error(int code, String message) {
            return ApiResponse.<T>builder().code(code).message(message).build();
        }
    }
}
