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
        // Đảm bảo status mặc định là FREE
        if (table.getStatus() == null || table.getStatus().isBlank()) {
            table.setStatus("FREE");
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
        if (payload.getStatus() != null && !payload.getStatus().isBlank()) {
            existing.setStatus(payload.getStatus().toUpperCase());
        }
        if (payload.getNote() != null)
            existing.setNote(payload.getNote());

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
        if (!tableRepository.existsById(tableId)) {
            throw new RuntimeException("Không tìm thấy bàn ID: " + tableId);
        }
        tableRepository.deleteById(tableId);
    }
}
