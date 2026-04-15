package com.example.nhom5.auth.model

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("user") val user: UserDto?
)

