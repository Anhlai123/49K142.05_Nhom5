package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;

public class CreateCustomerRequest {
    @SerializedName("name")
    private final String name;

    @SerializedName("phone")
    private final String phone;

    @SerializedName("email")
    private final String email;

    @SerializedName("notes")
    private final String notes;

    public CreateCustomerRequest(String name, String phone, String email, String notes) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
    }
}

