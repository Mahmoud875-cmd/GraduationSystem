package com.example.graduationsystem.data.model;

import com.google.gson.annotations.SerializedName;

/** Item of GET /api/admin/users (AdminController@users). */
public class AdminUser {
    @SerializedName("id") public int id;
    @SerializedName("name") public String name;
    @SerializedName("email") public String email;
    @SerializedName("role") public String role;
    @SerializedName("created_at") public String createdAt;
}
