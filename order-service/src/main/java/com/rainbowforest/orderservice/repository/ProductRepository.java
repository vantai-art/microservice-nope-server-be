package com.rainbowforest.orderservice.repository;

import com.rainbowforest.orderservice.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Tìm theo productId (id từ product-catalog-service)
    Optional<Product> findByProductId(Long productId);
}