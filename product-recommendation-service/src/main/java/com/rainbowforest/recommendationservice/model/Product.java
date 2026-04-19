package com.rainbowforest.recommendationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
// Sửa toàn bộ javax sang jakarta
import jakarta.persistence.*; 
import java.util.List;

@Entity
@Table (name = "products")
public class Product {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "product_name")
    private String productName;

    @OneToMany (mappedBy = "product")
    @JsonIgnore
    // Sửa lỗi chính tả từ 'recomendations' thành 'recommendations' nếu bạn muốn chuẩn hóa
    private List<Recommendation> recommendations; 
    
    public Product() {
    }

    public Product(String productName, List<Recommendation> recommendations) {
        this.productName = productName;
        this.recommendations = recommendations;
    }

    // --- Getter & Setter ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }
}