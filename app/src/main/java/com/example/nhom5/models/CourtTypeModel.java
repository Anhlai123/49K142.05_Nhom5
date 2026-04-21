package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;

public class CourtTypeModel {
    @SerializedName("id")
    private Integer id; // Dùng Integer để có thể null

    @SerializedName("type_name")
    private String typeName;

    @SerializedName("code")
    private String code;

    @SerializedName("name")
    private String name;

    @SerializedName("duration")
    private Integer duration;

    @SerializedName("status")
    private String status;

    public CourtTypeModel() {}

    public CourtTypeModel(Integer id, String typeName, String status) {
        this.id = id;
        this.typeName = typeName;
        this.status = status;
    }

    // Constructor dùng để thêm mới (chỉ cần name và duration)
    public CourtTypeModel(String name, Integer duration) {
        this.name = name;
        this.duration = duration;
    }

    public CourtTypeModel(String code, String name, Integer duration, String status) {
        this.code = code;
        this.name = name;
        this.duration = duration;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
