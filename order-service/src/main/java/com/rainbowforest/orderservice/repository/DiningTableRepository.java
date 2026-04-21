package com.rainbowforest.orderservice.repository;

import com.rainbowforest.orderservice.domain.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {

    Optional<DiningTable> findByNumber(Integer number);

    List<DiningTable> findByStatus(String status);
}
