package com.rainbowforest.productcatalogservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rainbowforest.productcatalogservice.entity.ActivityLog;
import com.rainbowforest.productcatalogservice.entity.Product;
import com.rainbowforest.productcatalogservice.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository repo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Ghi log ──────────────────────────────────────────────────

    public void logAdd(String performedBy, String role, Product product) {
        ActivityLog log = new ActivityLog();
        log.setPerformedBy(performedBy);
        log.setRole(role);
        log.setAction("ADD");
        log.setProductId(product.getId());
        log.setProductName(product.getProductName());
        log.setOldData(null);
        log.setNewData(toJson(product));
        repo.save(log);
    }

    public void logUpdate(String performedBy, String role, Product before, Product after) {
        ActivityLog log = new ActivityLog();
        log.setPerformedBy(performedBy);
        log.setRole(role);
        log.setAction("UPDATE");
        log.setProductId(after.getId());
        log.setProductName(after.getProductName());
        log.setOldData(toJson(before));
        log.setNewData(toJson(after));
        repo.save(log);
    }

    public void logDelete(String performedBy, String role, Product product) {
        ActivityLog log = new ActivityLog();
        log.setPerformedBy(performedBy);
        log.setRole(role);
        log.setAction("DELETE");
        log.setProductId(product.getId());
        log.setProductName(product.getProductName());
        log.setOldData(toJson(product));
        log.setNewData(null);
        repo.save(log);
    }

    // ── Truy vấn log ─────────────────────────────────────────────

    public List<ActivityLog> getAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public List<ActivityLog> getByPerformedBy(String performedBy) {
        return repo.findByPerformedByOrderByCreatedAtDesc(performedBy);
    }

    public List<ActivityLog> getByAction(String action) {
        return repo.findByActionOrderByCreatedAtDesc(action.toUpperCase());
    }

    public List<ActivityLog> getByProductId(Long productId) {
        return repo.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public List<ActivityLog> getByRole(String role) {
        return repo.findByRoleOrderByCreatedAtDesc(role.toUpperCase());
    }

    public List<ActivityLog> getByDateRange(LocalDateTime from, LocalDateTime to) {
        return repo.findByDateRange(from, to);
    }

    // ── Helper ───────────────────────────────────────────────────

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}