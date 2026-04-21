package com.rainbowforest.orderservice.http.request;

import java.util.List;
import java.util.Map;

public class TableOrderRequest {

    private Long tableId;
    private String customerName;
    private List<Map<String, Object>> items;

    public TableOrderRequest() {
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }
}