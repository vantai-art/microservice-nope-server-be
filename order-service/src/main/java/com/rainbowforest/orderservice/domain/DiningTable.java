package com.rainbowforest.orderservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity bàn - dùng field "number" và status "FREE"/"OCCUPIED"/"RESERVED"
 * để khớp với frontend React.
 */
@Entity
@Table(name = "dining_tables")
public class DiningTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Số bàn — khớp với field "number" ở frontend
     */
    @Column(name = "number", nullable = false, unique = true)
    @NotNull
    private Integer number;

    /**
     * Sức chứa (số người)
     */
    @Column(name = "capacity")
    private Integer capacity;

    /**
     * Trạng thái: FREE | OCCUPIED | RESERVED
     * (frontend dùng FREE thay vì AVAILABLE)
     */
    @Column(name = "status", nullable = false)
    @NotNull
    private String status = "FREE";

    /**
     * Ghi chú vị trí (tầng 1, ngoài trời...)
     */
    @Column(name = "note")
    private String note;

    /**
     * Key định danh dạng string: "table_1", "table_2"
     * Dùng để frontend nhận diện bàn qua socket/store
     */
    @Column(name = "table_key", unique = true)
    private String tableKey;

    /**
     * QR token (HMAC-signed) để khách scan vào đặt món
     */
    @Column(name = "qr_token", length = 512)
    private String qrToken;

    /**
     * Thời gian hết hạn của QR token
     */
    @Column(name = "qr_expires_at")
    private LocalDateTime qrExpiresAt;

    // ── Constructors ────────────────────────────────────────────────

    public DiningTable() {
    }

    public DiningTable(Integer number, Integer capacity) {
        this.number = number;
        this.capacity = capacity;
        this.status = "FREE";
        this.tableKey = "table_" + number;
    }

    // ── Getters & Setters ───────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTableKey() {
        return tableKey;
    }

    public void setTableKey(String tableKey) {
        this.tableKey = tableKey;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public LocalDateTime getQrExpiresAt() {
        return qrExpiresAt;
    }

    public void setQrExpiresAt(LocalDateTime qrExpiresAt) {
        this.qrExpiresAt = qrExpiresAt;
    }
}