package com.example.graduationsystem.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.graduationsystem.util.LocaleManager;
import com.example.graduationsystem.util.ThemeManager;

/**
 * Base activity that applies the persisted app language and theme to every screen.
 * All activities extend this so switching language / day-night affects the whole app.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        ThemeManager.applyFromContext(newBase);
        super.attachBaseContext(LocaleManager.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
