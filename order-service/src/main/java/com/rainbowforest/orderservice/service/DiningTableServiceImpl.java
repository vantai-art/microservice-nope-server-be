package com.rainbowforest.orderservice.service;

import com.rainbowforest.orderservice.domain.DiningTable;
import com.rainbowforest.orderservice.repository.DiningTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiningTableServiceImpl implements DiningTableService {

    @Autowired
    private DiningTableRepository tableRepository;

    @Override
    public List<DiningTable> getAllTables() {
        return tableRepository.findAll();
    }

    @Override
    public DiningTable getTableById(Long id) {
        return tableRepository.findById(id).orElse(null);
    }

    @Override
    public List<DiningTable> getTablesByStatus(String status) {
        return tableRepository.findByStatus(status.toUpperCase());
    }

    @Override
    public DiningTable createTable(DiningTable table) {
        if (table.getStatus() == null || table.getStatus().isBlank()) {
            table.setStatus("FREE");
        }
        // Auto tạo tableKey từ number nếu chưa có
        if (table.getTableKey() == null || table.getTableKey().isBlank()) {
            String key = table.getNumber() != null
                    ? "table_" + table.getNumber()
                    : "table_" + System.currentTimeMillis();
            table.setTableKey(key);
        }
        return tableRepository.save(table);
    }

    @Override
    public DiningTable updateTable(Long tableId, DiningTable payload) {
        DiningTable existing = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ID: " + tableId));

        if (payload.getNumber() != null)
            existing.setNumber(payload.getNumber());
        if (payload.getCapacity() != null)
            existing.setCapacity(payload.getCapacity());
        if (payload.getStatus() != null && !payload.getStatus().isBlank())
            existing.setStatus(payload.getStatus().toUpperCase());
        if (payload.getNote() != null)
            existing.setNote(payload.getNote());
        // Cập nhật tableKey nếu number thay đổi
        if (payload.getNumber() != null)
            existing.setTableKey("table_" + payload.getNumber());

        return tableRepository.save(existing);
    }

    @Override
    public DiningTable updateTableStatus(Long tableId, String status) {
        DiningTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ID: " + tableId));
        table.setStatus(status.toUpperCase());
        return tableRepository.save(table);
    }

    @Override
    public void deleteTable(Long tableId) {
        if (!tableRepository.existsById(tableId))
            throw new RuntimeException("Không tìm thấy bàn ID: " + tableId);
        tableRepository.deleteById(tableId);
    }
}