package com.example.graduationsystem.ui.student;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;

import com.example.graduationsystem.ui.BaseActivity;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.DashboardData;
import com.example.graduationsystem.data.model.User;
import com.example.graduationsystem.databinding.ActivityRegisterProjectBinding;
import com.example.graduationsystem.util.ApiUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Registers a project topic and picks a supervisor from the professors list. Student only. */
public class RegisterProjectActivity extends BaseActivity {

    private ActivityRegisterProjectBinding b;
    private final List<User> professors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRegisterProjectBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.btnBack.setOnClickListener(v -> finish());
        b.btnCancel.setOnClickListener(v -> finish());
        b.btnSubmit.setOnClickListener(v -> submit());
        b.inputDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateCharCounter(s.length());
            }
        });
        updateCharCounter(0);
        loadProfessors();
    }

    /** Mirrors the Stitch "۰ / ۵۰۰ نویسه" counter, turning red at the limit. */
    private void updateCharCounter(int length) {
        b.charCounter.setText(toPersianDigits(length) + " / " + toPersianDigits(500) + " " + getString(R.string.chars_unit));
        int color = length >= 500
                ? androidx.core.content.ContextCompat.getColor(this, R.color.error)
                : androidx.core.content.ContextCompat.getColor(this, R.color.outline);
        b.charCounter.setTextColor(color);
    }

    private static String toPersianDigits(long value) {
        String ascii = Long.toString(value);
        StringBuilder sb = new StringBuilder(ascii.length());
        for (int i = 0; i < ascii.length(); i++) {
            char c = ascii.charAt(i);
            sb.append(Character.isDigit(c) ? (char) ('۰' + (c - '0')) : c);
        }
        return sb.toString();
    }

    private void loadProfessors() {
        ApiClient.get(this).professors().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                ApiResponse<List<User>> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    professors.clear();
                    professors.addAll(body.data);
                    List<String> names = new ArrayList<>();
                    for (User u : professors) names.add(u.name);
                    b.dropdownSupervisor.setAdapter(new ArrayAdapter<>(RegisterProjectActivity.this,
                            android.R.layout.simple_list_item_1, names));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                ApiUtils.toast(RegisterProjectActivity.this, getString(R.string.network_error));
            }
        });
    }

    private void submit() {
        String title = b.inputTitle.getText().toString().trim();
        String description = b.inputDescription.getText().toString().trim();
        if (title.isEmpty() || description.isEmpty()) {
            ApiUtils.toast(this, getString(R.string.label_project_title));
            return;
        }
        int professorId = resolveProfessorId();
        if (professorId == 0) {
            ApiUtils.toast(this, getString(R.string.choose_supervisor));
            return;
        }

        setLoading(true);
        ApiClient.get(this).createProject(title, description, professorId)
                .enqueue(new Callback<ApiResponse<DashboardData.Project>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<DashboardData.Project>> call, Response<ApiResponse<DashboardData.Project>> response) {
                        setLoading(false);
                        ApiResponse<DashboardData.Project> body = response.body();
                        if (response.isSuccessful() && body != null && body.data != null) {
                            ApiUtils.toast(RegisterProjectActivity.this, getString(R.string.register_project_topic));
                            finish();
                        } else {
                            ApiUtils.toast(RegisterProjectActivity.this,
                                    (body != null && body.message != null) ? body.message : getString(R.string.generic_error));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<DashboardData.Project>> call, Throwable t) {
                        setLoading(false);
                        ApiUtils.toast(RegisterProjectActivity.this, getString(R.string.network_error));
                    }
                });
    }

    private int resolveProfessorId() {
        CharSequence text = b.dropdownSupervisor.getText();
        if (text == null) return 0;
        String label = text.toString();
        for (User u : professors) {
            if (u.name.equals(label)) return u.id;
        }
        return 0;
    }

    private void setLoading(boolean loading) {
        b.btnSubmit.setEnabled(!loading);
        b.btnSubmit.setText(loading ? R.string.loading : R.string.register_project_topic);
    }
}
