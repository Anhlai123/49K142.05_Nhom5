package com.example.nhom5.auth.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("message")
    private String message;
    
    @SerializedName("user")
    private UserDto user;

    @SerializedName("token")
    private String token;

    public String getMessage() { return message; }
    public UserDto getUser() { return user; }
    public String getToken() { return token; }
}
