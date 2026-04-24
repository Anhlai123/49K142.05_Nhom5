package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;

public class CustomerApiModel {
    @SerializedName("id")
    private Integer id;

    @SerializedName(value = "code", alternate = {"customer_code"})
    private String code;

    @SerializedName(value = "name", alternate = {"full_name"})
    private String name;

    @SerializedName(value = "phone", alternate = {"phone_number"})
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("notes")
    private String notes;

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getNotes() {
        return notes;
    }
}
