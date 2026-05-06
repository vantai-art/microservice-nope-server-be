package com.rainbowforest.orderservice.controller;

import com.rainbowforest.orderservice.domain.DiningTable;
import com.rainbowforest.orderservice.service.DiningTableService;
import com.rainbowforest.orderservice.service.QRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tables")
public class DiningTableController {

    @Autowired
    private DiningTableService tableService;

    @Autowired
    private QRService qrService;

    @Autowired
    private SimpMessagingTemplate ws;

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
    // GET /tables/available
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
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<DiningTable> updateTable(
            @PathVariable Long id,
            @RequestBody DiningTable payload) {
        try {
            DiningTable updated = tableService.updateTable(id, payload);
            // Broadcast real-time khi bàn thay đổi
            ws.convertAndSend("/topic/table/" + id,
                    Map.of("type", "table:updated", "tableId", id, "data", updated));
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─────────────────────────────────────────────
    // PUT /tables/{id}/status
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
            // Broadcast real-time
            ws.convertAndSend("/topic/table/" + id,
                    Map.of("type", "table:status_changed", "tableId", id, "data", updated));
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

    // ─────────────────────────────────────────────
    // GET /tables/{id}/qr → tạo QR URL cho bàn
    // ─────────────────────────────────────────────
    @GetMapping("/{id}/qr")
    public ResponseEntity<Map<String, String>> getQRCode(@PathVariable Long id) {
        try {
            String qrUrl = qrService.generateQRUrl(id);
            Map<String, String> result = new HashMap<>();
            result.put("qrUrl", qrUrl);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─────────────────────────────────────────────
    // GET /tables/qr/{token} → khách scan QR → lấy info bàn
    // ─────────────────────────────────────────────
    @GetMapping("/qr/{token}")
    public ResponseEntity<DiningTable> resolveQR(@PathVariable String token) {
        try {
            DiningTable table = qrService.resolveTable(token);
            return ResponseEntity.ok(table);
        } catch (IllegalStateException e) {
            // QR hết hạn → 410 Gone
            return ResponseEntity.status(410).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}