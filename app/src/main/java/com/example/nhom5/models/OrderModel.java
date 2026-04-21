package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;

public class OrderModel {
    @SerializedName("id")
    private int id;

    @SerializedName("order_code")
    private String orderCode;

    @SerializedName("customer_name")
    private String customerName;

    @SerializedName("status")
    private String status;

    public OrderModel() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
