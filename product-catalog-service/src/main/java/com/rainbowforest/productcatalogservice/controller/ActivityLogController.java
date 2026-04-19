package com.rainbowforest.productcatalogservice.controller;

import com.rainbowforest.productcatalogservice.entity.ActivityLog;
import com.rainbowforest.productcatalogservice.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Endpoints xem log hành động:
 *
 * GET /admin/logs → Tất cả log
 * GET /admin/logs?action=ADD → Lọc theo hành động (ADD/UPDATE/DELETE)
 * GET /admin/logs?performedBy=admin1 → Lọc theo người thực hiện
 * GET /admin/logs?role=ADMIN → Lọc theo role
 * GET /admin/logs?productId=5 → Lọc theo sản phẩm
 * GET /admin/logs?from=...&to=... → Lọc theo khoảng thời gian
 */
@RestController
@RequestMapping("/admin/logs")
@CrossOrigin(origins = "*")
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLog>> getLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        if (action != null) {
            return ResponseEntity.ok(activityLogService.getByAction(action));
        }
        if (performedBy != null) {
            return ResponseEntity.ok(activityLogService.getByPerformedBy(performedBy));
        }
        if (role != null) {
            return ResponseEntity.ok(activityLogService.getByRole(role));
        }
        if (productId != null) {
            return ResponseEntity.ok(activityLogService.getByProductId(productId));
        }
        if (from != null && to != null) {
            return ResponseEntity.ok(activityLogService.getByDateRange(from, to));
        }

        return ResponseEntity.ok(activityLogService.getAll());
    }
}