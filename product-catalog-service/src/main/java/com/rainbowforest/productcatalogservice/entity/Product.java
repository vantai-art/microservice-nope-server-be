package com.rainbowforest.productcatalogservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*; // Thay đổi ở đây
import jakarta.validation.constraints.NotNull; // Thay đổi ở đây
import java.math.BigDecimal;

@Entity
@Table (name = "products")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "product_name")
    @NotNull
    private String productName;

    @Column (name = "price")
    @NotNull
    private BigDecimal price;

    @Column (name = "discription") // Giữ nguyên typo "discription" theo code cũ của bạn để tránh lỗi DB
    private String discription;

    @Column (name = "category")
    @NotNull
    private String category;

    @Column (name = "availability")
    @NotNull
    private int availability;

    public Product() {
    }

    // --- Getter và Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getDiscription() { return discription; }
    public void setDiscription(String discription) { this.discription = discription; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getAvailability() { return availability; }
    public void setAvailability(int availability) { this.availability = availability; }
}