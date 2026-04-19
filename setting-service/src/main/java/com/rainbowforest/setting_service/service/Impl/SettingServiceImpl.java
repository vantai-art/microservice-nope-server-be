package com.rainbowforest.setting_service.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rainbowforest.setting_service.entity.AppSetting;
import com.rainbowforest.setting_service.entity.AppSetting.SettingScope;
import com.rainbowforest.setting_service.repository.AppSettingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rainbowforest.setting_service.service.SettingService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class SettingServiceImpl implements SettingService {

    @Autowired
    private AppSettingRepository repo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Serialize / Deserialize helpers ──────────────────────────

    private String serialize(Object value) {
        if (value == null)
            return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private Object deserialize(String raw) {
        if (raw == null)
            return null;
        try {
            // Try to parse as JSON (handles booleans, numbers, objects)
            return objectMapper.readValue(raw, Object.class);
        } catch (Exception e) {
            return raw; // Return as plain string if not valid JSON
        }
    }

    // ── Đọc settings ─────────────────────────────────────────────

    @Override
    public Map<String, Object> getGlobalSettings() {
        List<AppSetting> list = repo.findByScope(SettingScope.GLOBAL);
        return toMap(list);
    }

    @Override
    public Map<String, Object> getUserSettings(SettingScope scope, Long userId) {
        List<AppSetting> list = repo.findByScopeAndUserId(scope, userId);
        Map<String, Object> result = new LinkedHashMap<>();

        // Trộn default + user-specific (user override default)
        Map<String, Object> defaults = getDefaultsForScope(scope);
        result.putAll(defaults);
        result.putAll(toMap(list)); // user settings override defaults
        return result;
    }

    @Override
    public Object getSettingValue(SettingScope scope, Long userId, String key) {
        Optional<AppSetting> opt;
        if (userId != null) {
            opt = repo.findByScopeAndUserIdAndSettingKey(scope, userId, key);
        } else {
            opt = repo.findByScopeAndSettingKey(scope, key);
        }
        return opt.map(s -> deserialize(s.getSettingValue())).orElse(null);
    }

    // ── Ghi settings ─────────────────────────────────────────────

    @Override
    public Map<String, Object> saveGlobalSettings(Map<String, Object> settings) {
        settings.forEach((key, value) -> upsertSetting(SettingScope.GLOBAL, null, key, value, null));
        return getGlobalSettings();
    }

    @Override
    public Map<String, Object> saveUserSettings(SettingScope scope, Long userId, Map<String, Object> settings) {
        settings.forEach((key, value) -> upsertSetting(scope, userId, key, value, null));
        return getUserSettings(scope, userId);
    }

    @Override
    public void upsertSetting(SettingScope scope, Long userId, String key, Object value, String description) {
        Optional<AppSetting> existing;
        if (userId != null) {
            existing = repo.findByScopeAndUserIdAndSettingKey(scope, userId, key);
        } else {
            existing = repo.findByScopeAndSettingKey(scope, key);
        }

        AppSetting setting = existing.orElseGet(() -> {
            AppSetting s = new AppSetting();
            s.setSettingKey(key);
            s.setScope(scope);
            s.setUserId(userId);
            return s;
        });

        setting.setSettingValue(serialize(value));
        if (description != null)
            setting.setDescription(description);
        repo.save(setting);
    }

    // ── Xóa settings ─────────────────────────────────────────────

    @Override
    public void resetUserSettings(SettingScope scope, Long userId) {
        repo.deleteByUserIdAndScope(userId, scope);
    }

    @Override
    public void deleteSetting(SettingScope scope, Long userId, String key) {
        Optional<AppSetting> opt;
        if (userId != null) {
            opt = repo.findByScopeAndUserIdAndSettingKey(scope, userId, key);
        } else {
            opt = repo.findByScopeAndSettingKey(scope, key);
        }
        opt.ifPresent(repo::delete);
    }

    // ── Init defaults ─────────────────────────────────────────────

    @Override
    @PostConstruct
    public void initDefaultGlobalSettings() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("storeName", "Coffee Blend");
        defaults.put("storeEmail", "contact@coffeeblend.vn");
        defaults.put("storePhone", "0328778198");
        defaults.put("storeAddress", "2/60, Thủ Đức, TP.HCM");
        defaults.put("storeDescription", "Cà phê ngon, không gian đẹp");
        defaults.put("currency", "VND");
        defaults.put("timezone", "Asia/Ho_Chi_Minh");
        defaults.put("language", "vi");
        defaults.put("taxRate", 10.0);
        defaults.put("freeShippingThreshold", 200000.0);
        defaults.put("shippingFee", 30000.0);
        defaults.put("openTime", "07:00");
        defaults.put("closeTime", "22:00");
        defaults.put("themeColor", "#D97706");
        defaults.put("darkMode", false);

        // Chỉ insert nếu chưa tồn tại
        defaults.forEach((key, value) -> {
            Optional<AppSetting> existing = repo.findByScopeAndSettingKey(SettingScope.GLOBAL, key);
            if (existing.isEmpty()) {
                upsertSetting(SettingScope.GLOBAL, null, key, value, null);
            }
        });
    }

    @Override
    public void initDefaultUserSettings(SettingScope scope, Long userId) {
        Map<String, Object> defaults = getDefaultsForScope(scope);
        defaults.forEach((key, value) -> {
            Optional<AppSetting> existing = repo.findByScopeAndUserIdAndSettingKey(scope, userId, key);
            if (existing.isEmpty()) {
                upsertSetting(scope, userId, key, value, null);
            }
        });
    }

    // ── Private helpers ───────────────────────────────────────────

    private Map<String, Object> toMap(List<AppSetting> list) {
        Map<String, Object> map = new LinkedHashMap<>();
        list.forEach(s -> map.put(s.getSettingKey(), deserialize(s.getSettingValue())));
        return map;
    }

    private Map<String, Object> getDefaultsForScope(SettingScope scope) {
        Map<String, Object> defaults = new LinkedHashMap<>();

        // Defaults chung cho mọi scope
        defaults.put("emailNotifications", true);
        defaults.put("orderNotifications", true);
        defaults.put("promotionNotifications", false);
        defaults.put("themeColor", "#D97706");
        defaults.put("darkMode", false);
        defaults.put("language", "vi");
        defaults.put("fontSize", "medium");

        switch (scope) {
            case USER -> {
                defaults.put("paymentAlerts", true);
                defaults.put("deliveryAlerts", true);
                defaults.put("newsletterSubscribed", false);
            }
            case STAFF -> {
                defaults.put("soundEnabled", true);
                defaults.put("soundOnOrder", true);
                defaults.put("autoPrintReceipt", false);
                defaults.put("defaultLayout", "grid");
                defaults.put("itemsPerPage", 12);
                defaults.put("defaultPaymentMethod", "CASH");
                defaults.put("showTableMap", true);
                defaults.put("tablesPerRow", 4);
                defaults.put("lowStockAlerts", true);
                defaults.put("newOrderAlerts", true);
            }
            case ADMIN -> {
                defaults.put("lowStockAlerts", true);
                defaults.put("newOrderAlerts", true);
                defaults.put("paymentAlerts", true);
                defaults.put("reportEmailFrequency", "weekly");
                defaults.put("twoFactorEnabled", false);
                defaults.put("sessionTimeoutMinutes", 60);
            }
            default -> {
            }
        }

        return defaults;
    }
}