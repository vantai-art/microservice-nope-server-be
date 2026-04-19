package com.rainbowforest.settingservice.controller;

import com.rainbowforest.settingservice.dto.SettingDto;
import com.rainbowforest.settingservice.entity.AppSetting.SettingScope;
import com.rainbowforest.settingservice.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SettingController — quản lý cài đặt cho Admin, Staff, User
 *
 * Endpoints:
 *
 * ─── GLOBAL (Admin quản lý) ──────────────────────────────
 * GET /settings/global → Lấy tất cả cài đặt hệ thống
 * PUT /settings/global → Cập nhật cài đặt hệ thống
 *
 * ─── STORE INFO (Admin) ──────────────────────────────────
 * GET /settings/store → Lấy thông tin cửa hàng
 * PUT /settings/store → Cập nhật thông tin cửa hàng
 *
 * ─── USER SETTINGS ───────────────────────────────────────
 * GET /settings/user/{userId} → Lấy tất cả settings của 1 user
 * PUT /settings/user/{userId} → Cập nhật settings của user
 * DELETE /settings/user/{userId}/reset → Reset về mặc định
 * POST /settings/user/{userId}/init → Khởi tạo default lần đầu
 *
 * ─── STAFF SETTINGS ──────────────────────────────────────
 * GET /settings/staff/{staffId} → Lấy settings của staff
 * PUT /settings/staff/{staffId} → Cập nhật settings staff
 * DELETE /settings/staff/{staffId}/reset → Reset về mặc định
 * POST /settings/staff/{staffId}/init → Khởi tạo default lần đầu
 *
 * ─── ADMIN SETTINGS ──────────────────────────────────────
 * GET /settings/admin/{adminId} → Lấy settings của admin
 * PUT /settings/admin/{adminId} → Cập nhật settings admin
 *
 * ─── NOTIFICATIONS (tất cả role) ─────────────────────────
 * GET /settings/{scope}/{userId}/notifications
 * PUT /settings/{scope}/{userId}/notifications
 *
 * ─── APPEARANCE (tất cả role) ────────────────────────────
 * GET /settings/{scope}/{userId}/appearance
 * PUT /settings/{scope}/{userId}/appearance
 *
 * ─── POS (Staff only) ────────────────────────────────────
 * GET /settings/staff/{staffId}/pos
 * PUT /settings/staff/{staffId}/pos
 */
@RestController
@CrossOrigin(origins = "*")
public class SettingController {

    @Autowired
    private SettingService settingService;

    // ══════════════════════════════════════════════════════════════
    // GLOBAL Settings (Admin manages store-wide config)
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/settings/global")
    public ResponseEntity<SettingDto.SettingsResponse> getGlobalSettings() {
        Map<String, Object> data = settingService.getGlobalSettings();
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "OK", data));
    }

    @PutMapping("/settings/global")
    public ResponseEntity<SettingDto.SettingsResponse> updateGlobalSettings(
            @RequestBody SettingDto.BulkUpdateRequest req) {
        Map<String, Object> updated = settingService.saveGlobalSettings(req.getSettings());
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã lưu cài đặt hệ thống", updated));
    }

    // ── Alias cho FE AdminSettings.jsx (gọi /settings) ──────────
    @GetMapping("/settings")
    public ResponseEntity<SettingDto.SettingsResponse> getSettingsAlias() {
        return getGlobalSettings();
    }

    @PostMapping("/settings")
    public ResponseEntity<SettingDto.SettingsResponse> updateSettingsAlias(
            @RequestBody SettingDto.BulkUpdateRequest req) {
        return updateGlobalSettings(req);
    }

    // ══════════════════════════════════════════════════════════════
    // STORE Info (subset of GLOBAL)
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/settings/store")
    public ResponseEntity<SettingDto.SettingsResponse> getStoreSettings() {
        Map<String, Object> all = settingService.getGlobalSettings();
        // Filter chỉ lấy keys liên quan đến store
        String[] storeKeys = {
                "storeName", "storeEmail", "storePhone", "storeAddress",
                "storeDescription", "storeLogo", "currency", "timezone",
                "language", "taxRate", "freeShippingThreshold", "shippingFee",
                "openTime", "closeTime"
        };
        Map<String, Object> storeData = new java.util.LinkedHashMap<>();
        for (String k : storeKeys) {
            if (all.containsKey(k))
                storeData.put(k, all.get(k));
        }
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "OK", storeData));
    }

    @PutMapping("/settings/store")
    public ResponseEntity<SettingDto.SettingsResponse> updateStoreSettings(
            @RequestBody SettingDto.BulkUpdateRequest req) {
        Map<String, Object> updated = settingService.saveGlobalSettings(req.getSettings());
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã cập nhật thông tin cửa hàng", updated));
    }

    // ══════════════════════════════════════════════════════════════
    // USER Settings
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/settings/user/{userId}")
    public ResponseEntity<SettingDto.SettingsResponse> getUserSettings(@PathVariable Long userId) {
        Map<String, Object> data = settingService.getUserSettings(SettingScope.USER, userId);
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "OK", data));
    }

    @PutMapping("/settings/user/{userId}")
    public ResponseEntity<SettingDto.SettingsResponse> updateUserSettings(
            @PathVariable Long userId,
            @RequestBody SettingDto.BulkUpdateRequest req) {
        Map<String, Object> updated = settingService.saveUserSettings(SettingScope.USER, userId, req.getSettings());
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã lưu cài đặt người dùng", updated));
    }

    @PostMapping("/settings/user/{userId}/init")
    public ResponseEntity<SettingDto.SettingsResponse> initUserSettings(@PathVariable Long userId) {
        settingService.initDefaultUserSettings(SettingScope.USER, userId);
        Map<String, Object> data = settingService.getUserSettings(SettingScope.USER, userId);
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã khởi tạo cài đặt mặc định", data));
    }

    @DeleteMapping("/settings/user/{userId}/reset")
    public ResponseEntity<SettingDto.SettingsResponse> resetUserSettings(@PathVariable Long userId) {
        settingService.resetUserSettings(SettingScope.USER, userId);
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã đặt lại cài đặt mặc định", null));
    }

    // ══════════════════════════════════════════════════════════════
    // STAFF Settings
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/settings/staff/{staffId}")
    public ResponseEntity<SettingDto.SettingsResponse> getStaffSettings(@PathVariable Long staffId) {
        Map<String, Object> data = settingService.getUserSettings(SettingScope.STAFF, staffId);
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "OK", data));
    }

    @PutMapping("/settings/staff/{staffId}")
    public ResponseEntity<SettingDto.SettingsResponse> updateStaffSettings(
            @PathVariable Long staffId,
            @RequestBody SettingDto.BulkUpdateRequest req) {
        Map<String, Object> updated = settingService.saveUserSettings(SettingScope.STAFF, staffId, req.getSettings());
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã lưu cài đặt nhân viên", updated));
    }

    @PostMapping("/settings/staff/{staffId}/init")
    public ResponseEntity<SettingDto.SettingsResponse> initStaffSettings(@PathVariable Long staffId) {
        settingService.initDefaultUserSettings(SettingScope.STAFF, staffId);
        Map<String, Object> data = settingService.getUserSettings(SettingScope.STAFF, staffId);
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã khởi tạo cài đặt mặc định", data));
    }

    @DeleteMapping("/settings/staff/{staffId}/reset")
    public ResponseEntity<SettingDto.SettingsResponse> resetStaffSettings(@PathVariable Long staffId) {
        settingService.resetUserSettings(SettingScope.STAFF, staffId);
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã đặt lại cài đặt mặc định", null));
    }

    // ── POS Settings (Staff only) ────────────────────────────────
    @GetMapping("/settings/staff/{staffId}/pos")
    public ResponseEntity<SettingDto.SettingsResponse> getStaffPosSettings(@PathVariable Long staffId) {
        Map<String, Object> all = settingService.getUserSettings(SettingScope.STAFF, staffId);
        String[] posKeys = {
                "defaultLayout", "autoPrintReceipt", "soundOnOrder", "itemsPerPage",
                "defaultPaymentMethod", "showTableMap", "tablesPerRow"
        };
        Map<String, Object> pos = new java.util.LinkedHashMap<>();
        for (String k : posKeys) {
            if (all.containsKey(k))
                pos.put(k, all.get(k));
        }
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "OK", pos));
    }

    @PutMapping("/settings/staff/{staffId}/pos")
    public ResponseEntity<SettingDto.SettingsResponse> updateStaffPosSettings(
            @PathVariable Long staffId,
            @RequestBody SettingDto.BulkUpdateRequest req) {
        Map<String, Object> updated = settingService.saveUserSettings(SettingScope.STAFF, staffId, req.getSettings());
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã lưu cài đặt POS", updated));
    }

    // ══════════════════════════════════════════════════════════════
    // ADMIN Settings (per-admin preferences)
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/settings/admin/{adminId}")
    public ResponseEntity<SettingDto.SettingsResponse> getAdminSettings(@PathVariable Long adminId) {
        Map<String, Object> data = settingService.getUserSettings(SettingScope.ADMIN, adminId);
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "OK", data));
    }

    @PutMapping("/settings/admin/{adminId}")
    public ResponseEntity<SettingDto.SettingsResponse> updateAdminSettings(
            @PathVariable Long adminId,
            @RequestBody SettingDto.BulkUpdateRequest req) {
        Map<String, Object> updated = settingService.saveUserSettings(SettingScope.ADMIN, adminId, req.getSettings());
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã lưu cài đặt admin", updated));
    }

    // ══════════════════════════════════════════════════════════════
    // NOTIFICATIONS (generic endpoint cho mọi scope)
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/settings/{scope}/{userId}/notifications")
    public ResponseEntity<SettingDto.SettingsResponse> getNotificationSettings(
            @PathVariable String scope, @PathVariable Long userId) {
        SettingScope s = SettingScope.valueOf(scope.toUpperCase());
        Map<String, Object> all = settingService.getUserSettings(s, userId);
        String[] notiKeys = {
                "emailNotifications", "orderNotifications", "promotionNotifications",
                "lowStockAlerts", "newOrderAlerts", "paymentAlerts", "deliveryAlerts",
                "soundEnabled", "newsletterSubscribed"
        };
        Map<String, Object> noti = new java.util.LinkedHashMap<>();
        for (String k : notiKeys) {
            if (all.containsKey(k))
                noti.put(k, all.get(k));
        }
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "OK", noti));
    }

    @PutMapping("/settings/{scope}/{userId}/notifications")
    public ResponseEntity<SettingDto.SettingsResponse> updateNotificationSettings(
            @PathVariable String scope, @PathVariable Long userId,
            @RequestBody SettingDto.BulkUpdateRequest req) {
        SettingScope s = SettingScope.valueOf(scope.toUpperCase());
        Map<String, Object> updated = settingService.saveUserSettings(s, userId, req.getSettings());
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã lưu cài đặt thông báo", updated));
    }

    // ══════════════════════════════════════════════════════════════
    // APPEARANCE (generic endpoint cho mọi scope)
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/settings/{scope}/{userId}/appearance")
    public ResponseEntity<SettingDto.SettingsResponse> getAppearanceSettings(
            @PathVariable String scope, @PathVariable Long userId) {
        SettingScope s = SettingScope.valueOf(scope.toUpperCase());
        Map<String, Object> all = settingService.getUserSettings(s, userId);
        String[] appKeys = { "themeColor", "darkMode", "fontSize", "language" };
        Map<String, Object> app = new java.util.LinkedHashMap<>();
        for (String k : appKeys) {
            if (all.containsKey(k))
                app.put(k, all.get(k));
        }
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "OK", app));
    }

    @PutMapping("/settings/{scope}/{userId}/appearance")
    public ResponseEntity<SettingDto.SettingsResponse> updateAppearanceSettings(
            @PathVariable String scope, @PathVariable Long userId,
            @RequestBody SettingDto.BulkUpdateRequest req) {
        SettingScope s = SettingScope.valueOf(scope.toUpperCase());
        Map<String, Object> updated = settingService.saveUserSettings(s, userId, req.getSettings());
        return ResponseEntity.ok(new SettingDto.SettingsResponse(true, "Đã lưu cài đặt giao diện", updated));
    }

    // ══════════════════════════════════════════════════════════════
    // Health check
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/settings/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "setting-service"));
    }
}