package com.example.graduationsystem.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.example.graduationsystem.ui.BaseActivity;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.AuthData;
import com.example.graduationsystem.databinding.ActivityLoginBinding;
import com.example.graduationsystem.ui.admin.AdminDashboardActivity;
import com.example.graduationsystem.ui.professor.ProfessorDashboardActivity;
import com.example.graduationsystem.ui.student.StudentDashboardActivity;
import com.example.graduationsystem.util.ApiUtils;
import com.example.graduationsystem.util.LocaleManager;
import com.example.graduationsystem.util.Session;
import com.example.graduationsystem.util.ThemeManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding b;
    private boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        ApiClient.loadFromPrefs(this);

        Session session = Session.get(this);
        if (session.isLoggedIn()) {
            routeByRole(session.getRole());
            finish();
            return;
        }

        b.togglePassword.setOnClickListener(v -> togglePasswordVisibility());
        b.goRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        b.btnLogin.setOnClickListener(v -> doLogin());
        b.langButton.setOnClickListener(v -> showLanguageDialog());
        b.themeButton.setOnClickListener(v -> showThemeDialog());
        b.btnServerSettings.setOnClickListener(v -> showServerDialog());
        updateLangLabel();
        updateThemeLabel();
    }

    private void showServerDialog() {
        EditText input = new EditText(this);
        input.setHint(R.string.server_url_hint);
        input.setText(ApiClient.getBaseUrl());
        input.setSelection(input.getText().length());
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout wrap = new android.widget.FrameLayout(this);
        wrap.setPadding(pad, pad, pad, 0);
        wrap.addView(input);
        new AlertDialog.Builder(this)
                .setTitle(R.string.server_settings_title)
                .setView(wrap)
                .setPositiveButton(R.string.server_save, (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        ApiClient.saveBaseUrl(LoginActivity.this, url);
                        ApiUtils.toast(LoginActivity.this, getString(R.string.server_saved_toast));
                        recreate();
                    } else {
                        ApiUtils.toast(LoginActivity.this, getString(R.string.server_invalid_url));
                    }
                })
                .setNeutralButton(R.string.server_reset_default, (dialog, which) -> {
                    ApiClient.saveBaseUrl(LoginActivity.this, ApiClient.DEFAULT_BASE_URL);
                    ApiUtils.toast(LoginActivity.this, getString(R.string.server_saved_toast));
                    recreate();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateThemeLabel() {
        String mode = ThemeManager.getMode(this);
        int label;
        int icon;
        if (ThemeManager.MODE_LIGHT.equals(mode)) {
            label = R.string.theme_light;
            icon = R.drawable.ic_light_mode;
        } else if (ThemeManager.MODE_DARK.equals(mode)) {
            label = R.string.theme_dark;
            icon = R.drawable.ic_dark_mode;
        } else {
            label = R.string.theme_system;
            icon = R.drawable.ic_dark_mode;
        }
        b.themeLabel.setText(label);
        b.themeIcon.setImageResource(icon);
    }

    private void showThemeDialog() {
        final String[] codes = {ThemeManager.MODE_SYSTEM, ThemeManager.MODE_LIGHT, ThemeManager.MODE_DARK};
        CharSequence[] options = {
                getString(R.string.theme_system),
                getString(R.string.theme_light),
                getString(R.string.theme_dark)
        };
        String current = ThemeManager.getMode(this);
        int checked = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(current)) { checked = i; break; }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.appearance_title)
                .setSingleChoiceItems(options, checked, (dialog, which) -> {
                    dialog.dismiss();
                    String selected = codes[which];
                    if (!selected.equals(ThemeManager.getMode(LoginActivity.this))) {
                        ThemeManager.setMode(LoginActivity.this, selected);
                        recreate();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateLangLabel() {
        String lang = LocaleManager.getLanguage(this);
        int res;
        if ("ar".equals(lang)) res = R.string.lang_ar;
        else if ("en".equals(lang)) res = R.string.lang_en;
        else res = R.string.lang_fa;
        b.langLabel.setText(res);
    }

    private void showLanguageDialog() {
        final String[] codes = {"fa", "ar", "en"};
        CharSequence[] options = {
                getString(R.string.lang_fa),
                getString(R.string.lang_ar),
                getString(R.string.lang_en)
        };
        String current = LocaleManager.getLanguage(this);
        int checked = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(current)) { checked = i; break; }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.lang_dialog_title)
                .setSingleChoiceItems(options, checked, (dialog, which) -> {
                    dialog.dismiss();
                    String selected = codes[which];
                    if (!selected.equals(LocaleManager.getLanguage(LoginActivity.this))) {
                        LocaleManager.setLanguage(LoginActivity.this, selected);
                        recreate();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        b.inputPassword.setTransformationMethod(
                passwordVisible ? HideReturnsTransformationMethod.getInstance()
                        : PasswordTransformationMethod.getInstance());
        b.inputPassword.setSelection(b.inputPassword.getText().length());
    }

    private void doLogin() {
        String email = b.inputEmail.getText().toString().trim();
        String password = b.inputPassword.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            ApiUtils.toast(this, getString(R.string.network_error));
            return;
        }

        setLoading(true);
        ApiClient.get(this).login(email, password).enqueue(new Callback<ApiResponse<AuthData>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                setLoading(false);
                ApiResponse<AuthData> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    AuthData data = body.data;
                    Session.get(LoginActivity.this).saveAuth(
                            data.token, data.user.id, data.user.name, data.user.email, data.user.role);
                    routeByRole(data.user.role);
                    finish();
                } else {
                    String msg = (body != null && body.message != null)
                            ? body.message : getString(R.string.generic_error);
                    ApiUtils.toast(LoginActivity.this, msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                setLoading(false);
                ApiUtils.toast(LoginActivity.this, getString(R.string.network_error));
            }
        });
    }

    private void routeByRole(String role) {
        Class<?> target;
        if ("professor".equals(role)) target = ProfessorDashboardActivity.class;
        else if ("admin".equals(role)) target = AdminDashboardActivity.class;
        else target = StudentDashboardActivity.class;
        startActivity(new Intent(this, target));
    }

    private void setLoading(boolean loading) {
        b.btnLogin.setEnabled(!loading);
        b.btnLogin.setText(loading ? R.string.loading : R.string.login_action);
    }
}
