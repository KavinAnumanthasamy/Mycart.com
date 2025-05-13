package com.mycart.mycart.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StockDetails {
    private int availableStock;
    private String unitOfMeasure;
    private int soldOut;
    private int damaged;
//    private Integer soldOut;
//    private Integer damaged;
    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public int getSoldOut() {
        return soldOut;
    }

    public void setSoldOut(int soldOut) {
        this.soldOut = soldOut;
    }

    public int getDamaged() {
        return damaged;
    }

    public void setDamaged(int damaged) {
        this.damaged = damaged;
    }

    @Override
    public String toString() {
        return "StockDetails{" +
                "availableStock=" + availableStock +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", soldOut=" + soldOut +
                ", damaged=" + damaged +
                '}';
    }
}
