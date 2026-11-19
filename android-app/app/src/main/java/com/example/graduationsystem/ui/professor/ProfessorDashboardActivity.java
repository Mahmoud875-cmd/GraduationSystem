package com.example.graduationsystem.ui.professor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.graduationsystem.ui.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.DashboardData;
import com.example.graduationsystem.databinding.ActivityProfessorDashboardBinding;
import com.example.graduationsystem.ui.auth.LoginActivity;
import com.example.graduationsystem.ui.common.ReportsActivity;
import com.example.graduationsystem.ui.common.TasksActivity;
import com.example.graduationsystem.util.ApiUtils;
import com.example.graduationsystem.util.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Professor home: pending-topic count, review shortcut and active project list. */
public class ProfessorDashboardActivity extends BaseActivity implements ActiveProjectAdapter.Listener {

    private ActivityProfessorDashboardBinding b;
    private ActiveProjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProfessorDashboardBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        adapter = new ActiveProjectAdapter(this);
        b.projectsRecycler.setLayoutManager(new LinearLayoutManager(this));
        b.projectsRecycler.setAdapter(adapter);

        b.btnLogout.setOnClickListener(v -> doLogout());
        b.btnReview.setOnClickListener(v ->
                startActivity(new Intent(this, ReviewTopicsActivity.class)));
        b.pendingCard.setOnClickListener(v ->
                startActivity(new Intent(this, ReviewTopicsActivity.class)));
        // "View all" mirrors the review list for now: it is the only full project overview
        // a professor has (approved topics). Stitch shows no separate projects screen here.
        b.btnViewAll.setOnClickListener(v ->
                startActivity(new Intent(this, ReviewTopicsActivity.class)));

        b.swipeRefresh.setOnRefreshListener(this::load);
        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    bind(body.data);
                } else {
                    ApiUtils.toast(ProfessorDashboardActivity.this,
                            (body != null && body.message != null) ? body.message : getString(R.string.network_error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DashboardData>> call, Throwable t) {
                b.swipeRefresh.setRefreshing(false);
                ApiUtils.toast(ProfessorDashboardActivity.this, getString(R.string.network_error));
            }
        });
    }

    private void bind(DashboardData d) {
        b.welcomeText.setText(getString(R.string.welcome_prefix) + " " + d.name);
        // Stitch shows a localized date line under the greeting.
        b.greetingSubtext.setText(SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL,
                Locale.getDefault()).format(new Date()));
        b.pendingBody.setText(getString(R.string.prof_pending_banner_body_fmt, toPersianDigits(d.pendingCount)));

        List<DashboardData.Project> projects = d.activeProjects;
        int projectCount = projects == null ? 0 : projects.size();
        b.statProjectsCount.setText(toPersianDigits(projectCount));
        b.statPendingCount.setText(toPersianDigits(d.pendingCount));
        adapter.submit(projects);
        boolean empty = projects == null || projects.isEmpty();
        b.noProjectsText.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onTasks(DashboardData.Project project) {
        open(TasksActivity.class, project.id);
    }

    @Override
    public void onReports(DashboardData.Project project) {
        open(ReportsActivity.class, project.id);
    }

    @Override
    public void onEvaluate(DashboardData.Project project) {
        Intent i = new Intent(this, EvaluationActivity.class);
        i.putExtra("project_id", project.id);
        i.putExtra("project_title", project.title);
        startActivity(i);
    }

    private void open(Class<?> target, int projectId) {
        Intent i = new Intent(this, target);
        i.putExtra("project_id", projectId);
        startActivity(i);
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
