package com.example.graduationsystem.data.model;

import com.google.gson.annotations.SerializedName;

/** Minimal user object returned by /api/me, login and register. */
public class User {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("email")
    public String email;

    @SerializedName("role")
    public String role; // student | professor | admin

    public boolean isStudent() { return "student".equals(role); }
    public boolean isProfessor() { return "professor".equals(role); }
    public boolean isAdmin() { return "admin".equals(role); }
}
