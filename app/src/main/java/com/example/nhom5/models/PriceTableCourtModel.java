package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;

public class PriceTableCourtModel {
    @SerializedName("id")
    private int id;

    @SerializedName("price_table_id")
    private int priceTableId;

    @SerializedName("court_id")
    private int courtId;

    public PriceTableCourtModel() {}

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
    }

    public int getCourtId() {
        return courtId;
    }

    public void setCourtId(int courtId) {
        this.courtId = courtId;
    }
}
