package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;

public class PriceTableTimeSlotModel {
    @SerializedName("id")
    private int id;

    @SerializedName("price_table_id")
    private int priceTableId;
    private Integer id;

    @SerializedName("price_table")
    private Integer priceTable;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("price")
    private double price;

    public PriceTableTimeSlotModel() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPriceTableId() {
        return priceTableId;
    }

    public void setPriceTableId(int priceTableId) {
        this.priceTableId = priceTableId;
    @SerializedName("unit_price")
    private String unitPrice;

    @SerializedName("order")
    private Integer order;

    public PriceTableTimeSlotModel() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPriceTable() {
        return priceTable;
    }

    public void setPriceTable(Integer priceTable) {
        this.priceTable = priceTable;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
