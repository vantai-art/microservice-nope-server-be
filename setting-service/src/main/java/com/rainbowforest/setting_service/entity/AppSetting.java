package com.rainbowforest.settingservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Bảng lưu tất cả cài đặt hệ thống dạng key-value.
 *
 * scope = GLOBAL → áp dụng toàn hệ thống (admin quản lý), userId = null
 * scope = USER → riêng từng user, userId = id của user
 * scope = STAFF → riêng từng staff, userId = id của staff
 * scope = ADMIN → riêng từng admin, userId = id của admin
 */
@Entity
@Table(name = "app_settings", uniqueConstraints = @UniqueConstraint(columnNames = { "setting_key", "scope",
        "user_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên key, ví dụ: "storeName", "emailNotifications", "themeColor" */
    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;

    /** Giá trị lưu dạng String (số/boolean/JSON cũng stringify hết) */
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    /** Phạm vi áp dụng */
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 20)
    private SettingScope scope;

    /**
     * ID của user/staff/admin sở hữu setting này.
     * null nếu scope = GLOBAL.
     */
    @Column(name = "user_id")
    private Long userId;

    /** Mô tả ngắn cho key (optional, dùng khi debug) */
    @Column(name = "description", length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SettingScope {
        GLOBAL, USER, STAFF, ADMIN
    }
}