package com.example.graduationsystem.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.example.graduationsystem.ui.BaseActivity;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.AuthData;
import com.example.graduationsystem.databinding.ActivityRegisterBinding;
import com.example.graduationsystem.ui.admin.AdminDashboardActivity;
import com.example.graduationsystem.ui.professor.ProfessorDashboardActivity;
import com.example.graduationsystem.ui.student.StudentDashboardActivity;
import com.example.graduationsystem.util.ApiUtils;
import com.example.graduationsystem.util.Session;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new CharSequence[]{getString(R.string.role_student), getString(R.string.role_professor)});
        b.dropdownRole.setAdapter(adapter);

        b.goLogin.setOnClickListener(v -> finish());
        b.btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String name = b.inputName.getText().toString().trim();
        String email = b.inputEmail.getText().toString().trim();
        String password = b.inputPassword.getText().toString();
        String confirm = b.inputConfirm.getText().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            ApiUtils.toast(this, getString(R.string.network_error));
            return;
        }
        if (!password.equals(confirm)) {
            ApiUtils.toast(this, getString(R.string.label_confirm_password));
            return;
        }
        String role = resolveRole();
        if (role == null) {
            ApiUtils.toast(this, getString(R.string.choose_option));
            return;
        }

        Map<String, String> fields = new HashMap<>();
        fields.put("name", name);
        fields.put("email", email);
        fields.put("password", password);
        fields.put("password_confirmation", confirm);
        fields.put("role", role);

        setLoading(true);
        ApiClient.get(this).register(fields).enqueue(new Callback<ApiResponse<AuthData>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                setLoading(false);
                ApiResponse<AuthData> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    AuthData data = body.data;
                    Session.get(RegisterActivity.this).saveAuth(
                            data.token, data.user.id, data.user.name, data.user.email, data.user.role);
                    routeByRole(data.user.role);
                    finish();
                } else {
                    String msg = (body != null && body.message != null)
                            ? body.message : getString(R.string.generic_error);
                    ApiUtils.toast(RegisterActivity.this, msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                setLoading(false);
                ApiUtils.toast(RegisterActivity.this, getString(R.string.network_error));
            }
        });
    }

    private String resolveRole() {
        CharSequence text = b.dropdownRole.getText();
        if (text == null) return null;
        String label = text.toString();
        if (label.equals(getString(R.string.role_student))) return "student";
        if (label.equals(getString(R.string.role_professor))) return "professor";
        return null;
    }

    private void routeByRole(String role) {
        Class<?> target;
        if ("professor".equals(role)) target = ProfessorDashboardActivity.class;
        else if ("admin".equals(role)) target = AdminDashboardActivity.class;
        else target = StudentDashboardActivity.class;
        startActivity(new Intent(this, target));
    }

    private void setLoading(boolean loading) {
        b.btnRegister.setEnabled(!loading);
        b.btnRegister.setText(loading ? R.string.loading : R.string.register_link);
    }
}
