package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;

public class CourtTypeModel {
    @SerializedName("id")
    private int id;

    @SerializedName("type_name")
    private String typeName;

    @SerializedName("status")
    private String status;

    public CourtTypeModel() {}

    public CourtTypeModel(int id, String typeName, String status) {
        this.id = id;
        this.typeName = typeName;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
