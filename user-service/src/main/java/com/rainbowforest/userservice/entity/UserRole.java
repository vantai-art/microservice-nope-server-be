package com.rainbowforest.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
// Thay đổi quan trọng nhất ở đây
import jakarta.persistence.*; 
import java.util.List;

@Entity
@Table (name = "user_role")
@Data // Nếu dùng cái này, bạn có thể xóa mớ Getter/Setter ở dưới cho gọn
public class UserRole {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "role_name")
    private String roleName;

    @OneToMany (mappedBy = "role")
    @JsonIgnore
    private List<User> users;

    public UserRole() {
    }

    // --- Getter và Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
}