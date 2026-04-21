package com.rainbowforest.orderservice.service;

import com.rainbowforest.orderservice.domain.DiningTable;
import java.util.List;

public interface DiningTableService {

    List<DiningTable> getAllTables();

    DiningTable getTableById(Long id);

    List<DiningTable> getTablesByStatus(String status);

    DiningTable createTable(DiningTable table);

    /** Cập nhật toàn bộ thông tin bàn (number, capacity, status, note) */
    DiningTable updateTable(Long tableId, DiningTable payload);

    /** Chỉ cập nhật trạng thái bàn */
    DiningTable updateTableStatus(Long tableId, String status);

    void deleteTable(Long tableId);
}
