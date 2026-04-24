package com.example.nhom5.court;

import com.google.gson.annotations.SerializedName;

public class Court {
    @SerializedName("id")
    private Integer id;

    @SerializedName(value = "code", alternate = {"court_code"})
    private String code;

    @SerializedName("name")
    private String name;

    @SerializedName("court_type")
    private Integer courtTypeId;

    @SerializedName("court_type_name")
    private String type;

    @SerializedName("status")
    private String status;

    public Court() {}

    public Court(String name, Integer courtTypeId) {
        this.name = name;
        this.courtTypeId = courtTypeId;
    }

    public Court(String name, Integer courtTypeId, String status) {
        this.name = name;
        this.courtTypeId = courtTypeId;
        this.status = status;
    }

    public Court(int id, String name, String type, String status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCourtTypeId() { return courtTypeId; }
    public void setCourtTypeId(Integer courtTypeId) { this.courtTypeId = courtTypeId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
