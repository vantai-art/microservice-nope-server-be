package com.rainbowforest.orderservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordered_date")
    @NotNull
    private LocalDate orderedDate;

    @Column(name = "status")
    @NotNull
    private String status;

    @Column(name = "total")
    private BigDecimal total;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "order_items_mapping", joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    // =============================================
    // THÊM MỚI: Liên kết bàn & tên khách hàng
    // =============================================

    /**
     * Bàn mà order này thuộc về.
     * Nullable: trường hợp đặt online (delivery) thì không cần bàn.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private DiningTable diningTable;

    /**
     * Tên khách hàng do nhân viên điền vào (khách vãng lai không cần tài khoản).
     */
    @Column(name = "customer_name")
    private String customerName;

    public Order() {
    }

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public void setOrderedDate(LocalDate orderedDate) {
        this.orderedDate = orderedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DiningTable getDiningTable() {
        return diningTable;
    }

    public void setDiningTable(DiningTable diningTable) {
        this.diningTable = diningTable;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
