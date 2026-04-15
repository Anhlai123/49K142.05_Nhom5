package com.example.nhom5.auth.model;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private UserDto user;

    public String getMessage() {
        return message;
    }

    public UserDto getUser() {
        return user;
    }
}

