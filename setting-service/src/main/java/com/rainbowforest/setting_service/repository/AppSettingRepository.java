package com.rainbowforest.setting_service.repository;

import com.rainbowforest.setting_service.entity.AppSetting;
import com.rainbowforest.setting_service.entity.AppSetting.SettingScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {

    // ── Global settings ──────────────────────────────────────────
    List<AppSetting> findByScope(SettingScope scope);

    Optional<AppSetting> findByScopeAndSettingKey(SettingScope scope, String settingKey);

    // ── User / Staff / Admin specific settings ───────────────────
    List<AppSetting> findByScopeAndUserId(SettingScope scope, Long userId);

    Optional<AppSetting> findByScopeAndUserIdAndSettingKey(SettingScope scope, Long userId, String settingKey);

    // ── Xóa toàn bộ setting của 1 user ──────────────────────────
    @Modifying
    @Transactional
    @Query("DELETE FROM AppSetting s WHERE s.userId = :userId AND s.scope = :scope")
    void deleteByUserIdAndScope(@Param("userId") Long userId, @Param("scope") SettingScope scope);

    // ── Tìm theo key prefix (ví dụ: tất cả key bắt đầu bằng "notification") ──
    @Query("SELECT s FROM AppSetting s WHERE s.scope = :scope AND s.userId = :userId AND s.settingKey LIKE :prefix%")
    List<AppSetting> findByUserIdAndKeyPrefix(
            @Param("scope") SettingScope scope,
            @Param("userId") Long userId,
            @Param("prefix") String prefix);
}