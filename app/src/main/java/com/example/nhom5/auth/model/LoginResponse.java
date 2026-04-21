package com.example.nhom5.auth.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private UserDto user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
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
