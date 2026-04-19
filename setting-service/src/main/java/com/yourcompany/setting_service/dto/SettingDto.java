package com.rainbowforest.settingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * DTO dùng cho tất cả request/response của settings API.
 * FE gửi lên dạng Map<String, Object> để linh hoạt.
 */
public class SettingDto {

    /** Request: FE gửi map {key: value} */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkUpdateRequest {
        private Map<String, Object> settings;
    }

    /** Response: trả về map {key: value} dạng flat */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingsResponse {
        private boolean success;
        private String message;
        private Map<String, Object> data;
    }

    /** Single setting item */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingItem {
        private String key;
        private Object value;
        private String description;
    }

    // ─────────────────────────────────────────────────────────────
    // Typed DTOs cho từng nhóm cài đặt
    // ─────────────────────────────────────────────────────────────

    /** Cài đặt thông tin cửa hàng (GLOBAL - Admin only) */
    @Data
    @NoArgsConstructor
    public static class StoreSettings {
        private String storeName;
        private String storeEmail;
        private String storePhone;
        private String storeAddress;
        private String storeDescription;
        private String storeLogo;
        private String currency; // "VND", "USD"
        private String timezone; // "Asia/Ho_Chi_Minh"
        private String language; // "vi", "en"
        private Double taxRate; // 10.0 (%)
        private Double freeShippingThreshold;
        private Double shippingFee;
        private String openTime; // "07:00"
        private String closeTime; // "22:00"
    }

    /** Cài đặt thông báo (áp dụng cho mọi role) */
    @Data
    @NoArgsConstructor
    public static class NotificationSettings {
        private Boolean emailNotifications;
        private Boolean orderNotifications;
        private Boolean promotionNotifications;
        private Boolean lowStockAlerts; // Staff / Admin
        private Boolean newOrderAlerts; // Staff / Admin
        private Boolean paymentAlerts; // User
        private Boolean deliveryAlerts; // User
        private Boolean soundEnabled; // Staff POS
    }

    /** Cài đặt giao diện */
    @Data
    @NoArgsConstructor
    public static class AppearanceSettings {
        private String themeColor; // "#D97706"
        private Boolean darkMode;
        private String fontSize; // "small", "medium", "large"
        private String language;
    }

    /** Cài đặt profile người dùng */
    @Data
    @NoArgsConstructor
    public static class ProfileSettings {
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String street;
        private String streetNumber;
        private String zipCode;
        private String locality;
        private String country;
        private String avatar;
        private String bio;
    }

    /** Cài đặt bảo mật */
    @Data
    @NoArgsConstructor
    public static class SecuritySettings {
        private String currentPassword;
        private String newPassword;
        private Boolean twoFactorEnabled;
        private Boolean sessionTimeout;
        private Integer sessionTimeoutMinutes;
    }

    /** Cài đặt POS cho Staff */
    @Data
    @NoArgsConstructor
    public static class PosSettings {
        private String defaultLayout; // "grid", "list"
        private Boolean autoPrintReceipt;
        private Boolean soundOnOrder;
        private Integer itemsPerPage;
        private String defaultPaymentMethod; // "CASH", "MOMO", "VNPAY"
        private Boolean showTableMap;
        private Integer tablesPerRow;
    }
}