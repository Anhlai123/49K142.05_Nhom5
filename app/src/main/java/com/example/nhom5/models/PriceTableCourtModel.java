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
    private Integer id;

    @SerializedName("price_table")
    private Integer priceTableId;

    @SerializedName("court")
    private Integer courtId;

    @SerializedName("court_name")
    private String courtName;

    @SerializedName("court_code")
    private String courtCode;

    public PriceTableCourtModel() {}

    public PriceTableCourtModel(Integer priceTableId, Integer courtId) {
        this.priceTableId = priceTableId;
        this.courtId = courtId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPriceTableId() {
        return priceTableId;
    }

    public void setPriceTableId(Integer priceTableId) {
        this.priceTableId = priceTableId;
    }

    public Integer getCourtId() {
        return courtId;
    }

    public void setCourtId(Integer courtId) {
        this.courtId = courtId;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getCourtCode() {
        return courtCode;
    }

    public void setCourtCode(String courtCode) {
        this.courtCode = courtCode;
    }
}
