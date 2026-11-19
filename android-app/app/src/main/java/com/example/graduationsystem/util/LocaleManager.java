package com.example.graduationsystem.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Persists the user's chosen app language and applies it to every context.
 * Supported: fa (default, RTL), ar (RTL), en (LTR).
 */
public final class LocaleManager {
    private static final String PREFS = "gs_locale";
    private static final String KEY_LANG = "lang";
    public static final String DEFAULT_LANG = "fa";

    private LocaleManager() {}

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANG, DEFAULT_LANG);
        return (lang == null || lang.isEmpty()) ? DEFAULT_LANG : lang;
    }

    public static void setLanguage(Context context, String lang) {
        context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_LANG, lang).apply();
    }

    /** Wraps a base context with the stored locale so resources resolve in that language. */
    public static Context wrap(Context base) {
        String lang = getLanguage(base);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        return base.createConfigurationContext(config);
    }
}
