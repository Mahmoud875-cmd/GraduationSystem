package com.example.graduationsystem.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores the Sanctum token and current user in SharedPreferences.
 * Kept simple (plain prefs) for development reliability; can be swapped to EncryptedSharedPreferences later.
 */
public class Session {
    private static final String FILE = "gs_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private static Session instance;
    private final SharedPreferences prefs;

    private Session(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static synchronized Session get(Context context) {
        if (instance == null) {
            instance = new Session(context);
        }
        return instance;
    }

    public void saveAuth(String token, int userId, String name, String email, String role) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public int getUserId() { return prefs.getInt(KEY_USER_ID, 0); }
    public String getName() { return prefs.getString(KEY_NAME, ""); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, ""); }
    public String getRole() { return prefs.getString(KEY_ROLE, ""); }
    public boolean isLoggedIn() { return getToken() != null && !getToken().isEmpty(); }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
