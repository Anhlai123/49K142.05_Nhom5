package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;

public class PriceTableCourtModel {
    @SerializedName("id")
    private Integer id;

    @SerializedName("price_table_id")
    private Integer priceTableId;

    @SerializedName("court_id")
    private Integer courtId;

    @SerializedName("price_table")
    private Integer priceTable;

    @SerializedName("court")
    private Integer court;

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

    public Integer getPriceTable() {
        return priceTable;
    }

    public void setPriceTable(Integer priceTable) {
        this.priceTable = priceTable;
    }

    public Integer getCourt() {
        return court;
    }

    public void setCourt(Integer court) {
        this.court = court;
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
