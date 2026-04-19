package com.rainbowforest.setting_service.service;

import com.rainbowforest.setting_service.entity.AppSetting.SettingScope;
import java.util.Map;

public interface SettingService {

    // ── Đọc settings ─────────────────────────────────────────────

    /** Lấy tất cả GLOBAL settings dạng Map */
    Map<String, Object> getGlobalSettings();

    /** Lấy tất cả settings của 1 user theo scope */
    Map<String, Object> getUserSettings(SettingScope scope, Long userId);

    /** Lấy 1 setting value theo key */
    Object getSettingValue(SettingScope scope, Long userId, String key);

    // ── Ghi settings ─────────────────────────────────────────────

    /** Bulk upsert GLOBAL settings (Admin only) */
    Map<String, Object> saveGlobalSettings(Map<String, Object> settings);

    /** Bulk upsert settings cho 1 user */
    Map<String, Object> saveUserSettings(SettingScope scope, Long userId, Map<String, Object> settings);

    /** Upsert 1 setting key */
    void upsertSetting(SettingScope scope, Long userId, String key, Object value, String description);

    // ── Xóa settings ─────────────────────────────────────────────

    /** Xóa toàn bộ settings của 1 user (reset về default) */
    void resetUserSettings(SettingScope scope, Long userId);

    /** Xóa 1 setting cụ thể */
    void deleteSetting(SettingScope scope, Long userId, String key);

    // ── Helpers ───────────────────────────────────────────────────

    /** Khởi tạo default settings cho user mới */
    void initDefaultUserSettings(SettingScope scope, Long userId);

    /** Khởi tạo default GLOBAL settings (gọi lúc app start) */
    void initDefaultGlobalSettings();
}