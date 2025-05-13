package com.ust.Activemq.Model;

import java.math.BigDecimal;
import java.util.Map;

public class Item {
    private String id;
    private String name;
    private BigDecimal price;
    private int stockQuantity;
    private Map<String, Integer> stockDetails;

    // Getters and setters

    public Map<String, Integer> getStockDetails() {
        return stockDetails;
    }

    public void setStockDetails(Map<String, Integer> stockDetails) {
        this.stockDetails = stockDetails;
    }

    public Item() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Object getItemId() {
        return null;
    }

    public Object getInventoryList() {
        return null;
    }
}
