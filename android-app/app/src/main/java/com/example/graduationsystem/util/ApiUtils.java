package com.example.graduationsystem.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.model.ApiResponse;

import retrofit2.Response;

/** Helpers to interpret Retrofit responses and surface Persian error messages. */
public final class ApiUtils {
    private ApiUtils() {}

    /** True when the HTTP call succeeded (2xx) and the JSON body parsed into data. */
    public static boolean isSuccess(@Nullable Response<ApiResponse<?>> response) {
        return response != null && response.isSuccessful()
                && response.body() != null && response.body().data != null;
    }

    /** Extract a human message: server 'message' first, then a generic network/HTTP fallback. */
    public static String extractError(Context context, @Nullable Response<ApiResponse<?>> response) {
        if (response != null && response.body() != null && response.body().message != null
                && !response.body().message.isEmpty()) {
            return response.body().message;
        }
        if (response != null && response.code() == 401) {
            return context.getString(R.string.login_title); // session expired -> re-login handled by caller
        }
        return context.getString(R.string.network_error);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
