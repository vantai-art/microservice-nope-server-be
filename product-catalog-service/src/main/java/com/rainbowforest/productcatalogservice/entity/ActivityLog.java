package com.rainbowforest.productcatalogservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người thực hiện (admin/staff) — FE truyền lên qua header hoặc body
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    // Role: ADMIN hoặc STAFF
    @Column(name = "role", length = 20)
    private String role;

    // Hành động: ADD, UPDATE, DELETE
    @Column(name = "action", nullable = false, length = 20)
    private String action;

    // ID sản phẩm bị tác động
    @Column(name = "product_id")
    private Long productId;

    // Tên sản phẩm tại thời điểm thực hiện
    @Column(name = "product_name", length = 255)
    private String productName;

    // Dữ liệu trước khi sửa (JSON string) — null nếu là ADD
    @Column(name = "old_data", columnDefinition = "TEXT")
    private String oldData;

    // Dữ liệu sau khi sửa (JSON string) — null nếu là DELETE
    @Column(name = "new_data", columnDefinition = "TEXT")
    private String newData;

    // Ghi chú thêm
    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public ActivityLog() {
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getOldData() {
        return oldData;
    }

    public void setOldData(String oldData) {
        this.oldData = oldData;
    }

    public String getNewData() {
        return newData;
    }

    public void setNewData(String newData) {
        this.newData = newData;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}