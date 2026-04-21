package com.rainbowforest.orderservice.controller;

import com.rainbowforest.orderservice.domain.DiningTable;
import com.rainbowforest.orderservice.service.DiningTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API quản lý bàn.
 *
 * GET /tables → tất cả bàn
 * GET /tables/available → bàn đang trống (status=FREE)
 * GET /tables/{id} → 1 bàn
 * POST /tables → tạo bàn mới { number, capacity, note? }
 * PUT /tables/{id} → sửa toàn bộ bàn (dùng bởi StaffPage khi đổi OCCUPIED/FREE)
 * PUT /tables/{id}/status → chỉ đổi trạng thái { status: "FREE" }
 * DELETE /tables/{id} → xóa bàn
 */
@RestController
@RequestMapping("/tables")
public class DiningTableController {

    @Autowired
    private DiningTableService tableService;

    // ─────────────────────────────────────────────
    // GET /tables
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<DiningTable>> getAllTables() {
        List<DiningTable> tables = tableService.getAllTables();
        if (tables == null || tables.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tables);
    }

    // ─────────────────────────────────────────────
    // GET /tables/available → bàn FREE
    // ─────────────────────────────────────────────
    @GetMapping("/available")
    public ResponseEntity<List<DiningTable>> getAvailableTables() {
        return ResponseEntity.ok(tableService.getTablesByStatus("FREE"));
    }

    // ─────────────────────────────────────────────
    // GET /tables/{id}
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<DiningTable> getTableById(@PathVariable Long id) {
        DiningTable table = tableService.getTableById(id);
        if (table == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(table);
    }

    // ─────────────────────────────────────────────
    // POST /tables
    // Body: { "number": 1, "capacity": 4, "note": "Tầng 1" }
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<DiningTable> createTable(@RequestBody DiningTable table) {
        try {
            DiningTable saved = tableService.createTable(table);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ─────────────────────────────────────────────
    // PUT /tables/{id}
    // StaffPage gọi cái này để cập nhật toàn bộ bàn
    // Body: { "number": 3, "capacity": 4, "status": "OCCUPIED" }
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<DiningTable> updateTable(
            @PathVariable Long id,
            @RequestBody DiningTable payload) {
        try {
            DiningTable updated = tableService.updateTable(id, payload);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─────────────────────────────────────────────
    // PUT /tables/{id}/status
    // AdminTable gọi khi chỉ đổi trạng thái
    // Body: { "status": "FREE" }
    // ─────────────────────────────────────────────
    @PutMapping("/{id}/status")
    public ResponseEntity<DiningTable> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String s = status.toUpperCase();
        if (!s.equals("FREE") && !s.equals("OCCUPIED") && !s.equals("RESERVED")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            DiningTable updated = tableService.updateTableStatus(id, s);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /tables/{id}
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        try {
            tableService.deleteTable(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
