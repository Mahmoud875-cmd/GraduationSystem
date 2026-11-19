package com.example.graduationsystem.data.model;

import com.google.gson.annotations.SerializedName;

/** User + Sanctum token returned by POST /api/login and POST /api/register. */
public class AuthData {
    @SerializedName("token")
    public String token;

    @SerializedName("user")
    public User user;
}
