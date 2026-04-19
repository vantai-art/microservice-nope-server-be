package com.rainbowforest.recommendationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
// Chuyển từ javax sang jakarta
import jakarta.persistence.*; 
import java.util.List;

@Entity
@Table (name = "users")
public class User {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "user_name")
    private String userName;

    @OneToMany (mappedBy = "user")
    @JsonIgnore
    // Sửa lỗi chính tả từ 'recomendations' thành 'recommendations' (2 chữ m) để đồng bộ
    private List<Recommendation> recommendations;

    public User() {
    }

    public User(String userName, List<Recommendation> recommendations) {
        this.userName = userName;
        this.recommendations = recommendations;
    }

    // --- Getter & Setter ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }
}