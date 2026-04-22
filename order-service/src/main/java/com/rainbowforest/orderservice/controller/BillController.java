package com.rainbowforest.orderservice.controller;

import com.rainbowforest.orderservice.domain.Bill;
import com.rainbowforest.orderservice.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bills")
public class BillController {

    @Autowired
    private BillRepository billRepository;

    @GetMapping
    public ResponseEntity<List<Bill>> getAllBills() {
        List<Bill> bills = billRepository.findAll();
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        return billRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Bill> createBill(@RequestBody Bill bill) {
        bill.setCreatedAt(new java.util.Date());
        Bill saved = billRepository.save(bill);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Bill>> getBillsByOrderId(@PathVariable Long orderId) {
        List<Bill> bills = billRepository.findByOrderId(orderId);
        return ResponseEntity.ok(bills);
    }
}