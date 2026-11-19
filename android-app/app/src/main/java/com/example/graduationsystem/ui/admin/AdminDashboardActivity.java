package com.example.graduationsystem.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import com.example.graduationsystem.ui.BaseActivity;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.DashboardData;
import com.example.graduationsystem.databinding.ActivityAdminDashboardBinding;
import com.example.graduationsystem.ui.auth.LoginActivity;
import com.example.graduationsystem.util.ApiUtils;
import com.example.graduationsystem.util.Session;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Admin home: aggregate counts and shortcuts to users / projects management. */
public class AdminDashboardActivity extends BaseActivity {

    private ActivityAdminDashboardBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.btnLogout.setOnClickListener(v -> doLogout());
        b.cardUsers.setOnClickListener(v ->
                startActivity(new Intent(this, AdminUsersActivity.class)));
        b.cardProjects.setOnClickListener(v ->
                startActivity(new Intent(this, AdminProjectsActivity.class)));

        b.swipeRefresh.setOnRefreshListener(this::load);
        load();
    }

    private void load() {
        b.swipeRefresh.setRefreshing(true);
        ApiClient.get(this).dashboard().enqueue(new Callback<ApiResponse<DashboardData>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardData>> call, Response<ApiResponse<DashboardData>> response) {
                b.swipeRefresh.setRefreshing(false);
                ApiResponse<DashboardData> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    DashboardData d = body.data;
                    if (d.name != null && !d.name.isEmpty()) {
                        b.welcomeText.setText(getString(R.string.welcome_prefix) + "، " + d.name);
                    }
                    String users = toPersianDigits(d.usersCount);
                    String projects = toPersianDigits(d.projectsCount);
                    String pending = toPersianDigits(d.pendingProjectsCount);
                    b.greetingSummary.setText(getString(R.string.admin_summary_fmt, users, projects, pending));
                    b.pendingCount.setText(pending);
                    b.projectsCount.setText(projects);
                    b.usersBadge.setText(getString(R.string.admin_users_count_fmt, users));
                    b.projectsBadge.setText(getString(R.string.admin_projects_count_fmt, projects));
                } else {
                    ApiUtils.toast(AdminDashboardActivity.this,
                            (body != null && body.message != null) ? body.message : getString(R.string.network_error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DashboardData>> call, Throwable t) {
                b.swipeRefresh.setRefreshing(false);
                ApiUtils.toast(AdminDashboardActivity.this, getString(R.string.network_error));
            }
        });
    }

    private String toPersianDigits(int in) {
        String s = Integer.toString(in);
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append(c >= '0' && c <= '9' ? (char) ('۰' + (c - '0')) : c);
        }
        return sb.toString();
    }

    private void doLogout() {
        ApiClient.get(this).logout().enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                clearAndExit();
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                clearAndExit();
            }
        });
    }

    private void clearAndExit() {
        Session.get(this).clear();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
