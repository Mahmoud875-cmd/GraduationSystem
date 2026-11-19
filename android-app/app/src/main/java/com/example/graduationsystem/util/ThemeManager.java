package com.example.graduationsystem.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Persists the user's theme choice and applies it via AppCompatDelegate.
 * Modes: "system" (follow device), "light", "dark". Default follows the system.
 */
public final class ThemeManager {
    private static final String PREFS = "gs_theme";
    private static final String KEY_MODE = "mode";

    public static final String MODE_SYSTEM = "system";
    public static final String MODE_LIGHT = "light";
    public static final String MODE_DARK = "dark";

    private ThemeManager() {}

    public static String getMode(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String mode = prefs.getString(KEY_MODE, MODE_SYSTEM);
        return (mode == null || mode.isEmpty()) ? MODE_SYSTEM : mode;
    }

    public static void setMode(Context context, String mode) {
        context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_MODE, mode).apply();
        apply(mode);
    }

    /** Maps the stored mode to the AppCompat night mode and applies it process-wide. */
    public static void apply(String mode) {
        int nightMode;
        if (MODE_LIGHT.equals(mode)) nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        else if (MODE_DARK.equals(mode)) nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        else nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    /** Applies the persisted mode. Safe to call from every activity's base context. */
    public static void applyFromContext(Context context) {
        apply(getMode(context));
    }
}
