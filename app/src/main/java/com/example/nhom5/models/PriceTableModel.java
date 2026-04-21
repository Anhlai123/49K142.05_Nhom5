package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;

public class PriceTableModel {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    public PriceTableModel() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    private Integer id;

    @SerializedName("price_table_code")
    private String priceTableCode;

    @SerializedName("price_table_name")
    private String priceTableName;

    @SerializedName("court_type")
    private Integer courtType;

    @SerializedName("court_type_name")
    private String courtTypeName;

    @SerializedName("apply_scope")
    private String applyScope;

    @SerializedName("effective_date")
    private String effectiveDate;

    public PriceTableModel() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPriceTableCode() {
        return priceTableCode;
    }

    public void setPriceTableCode(String priceTableCode) {
        this.priceTableCode = priceTableCode;
    }

    public String getPriceTableName() {
        return priceTableName;
    }

    public void setPriceTableName(String priceTableName) {
        this.priceTableName = priceTableName;
    }

    public Integer getCourtType() {
        return courtType;
    }

    public void setCourtType(Integer courtType) {
        this.courtType = courtType;
    }

    public String getCourtTypeName() {
        return courtTypeName;
    }

    public void setCourtTypeName(String courtTypeName) {
        this.courtTypeName = courtTypeName;
    }

    public String getApplyScope() {
        return applyScope;
    }

    public void setApplyScope(String applyScope) {
        this.applyScope = applyScope;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
