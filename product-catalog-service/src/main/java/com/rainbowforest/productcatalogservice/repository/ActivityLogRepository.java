package com.rainbowforest.productcatalogservice.repository;

import com.rainbowforest.productcatalogservice.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // Lấy tất cả log, mới nhất trước
    List<ActivityLog> findAllByOrderByCreatedAtDesc();

    // Lọc theo người thực hiện
    List<ActivityLog> findByPerformedByOrderByCreatedAtDesc(String performedBy);

    // Lọc theo hành động (ADD, UPDATE, DELETE)
    List<ActivityLog> findByActionOrderByCreatedAtDesc(String action);

    // Lọc theo sản phẩm
    List<ActivityLog> findByProductIdOrderByCreatedAtDesc(Long productId);

    // Lọc theo khoảng thời gian
    @Query("SELECT l FROM ActivityLog l WHERE l.createdAt BETWEEN :from AND :to ORDER BY l.createdAt DESC")
    List<ActivityLog> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Lọc theo role
    List<ActivityLog> findByRoleOrderByCreatedAtDesc(String role);
}