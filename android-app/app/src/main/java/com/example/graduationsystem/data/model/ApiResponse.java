package com.example.graduationsystem.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Mirrors the { "data": ... } envelope used by every success response in the Laravel API,
 * and the { "message", "error_code", "errors" } shape used by failure responses.
 */
public class ApiResponse<T> {
    @SerializedName("data")
    public T data;

    @SerializedName("message")
    public String message;

    @SerializedName("error_code")
    public String errorCode;

    @SerializedName("errors")
    public java.util.Map<String, java.util.List<String>> errors;
}
