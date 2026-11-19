package com.example.graduationsystem.ui.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.example.graduationsystem.ui.BaseActivity;
import androidx.core.content.ContextCompat;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.DashboardData;
import com.example.graduationsystem.databinding.ActivityStudentDashboardBinding;
import com.example.graduationsystem.databinding.ItemTeamMemberBinding;
import com.example.graduationsystem.ui.auth.LoginActivity;
import com.example.graduationsystem.ui.common.ReportsActivity;
import com.example.graduationsystem.ui.common.TasksActivity;
import com.example.graduationsystem.util.ApiUtils;
import com.example.graduationsystem.util.Session;
import com.example.graduationsystem.util.StatusMapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Student home: shows team, project progress, evaluation and empty-state actions. */
public class StudentDashboardActivity extends BaseActivity {

    private ActivityStudentDashboardBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.btnLogout.setOnClickListener(v -> doLogout());
        b.btnCreateTeam.setOnClickListener(v ->
                startActivity(new Intent(this, CreateTeamActivity.class)));
        b.btnRegisterProject.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterProjectActivity.class)));

        b.swipeRefresh.setOnRefreshListener(this::loadDashboard);
        loadDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always reload so progress/status changes made in Tasks/Reports are reflected.
        loadDashboard();
    }

    private void loadDashboard() {
        b.swipeRefresh.setRefreshing(true);
        ApiClient.get(this).dashboard().enqueue(new Callback<ApiResponse<DashboardData>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardData>> call, Response<ApiResponse<DashboardData>> response) {
                b.swipeRefresh.setRefreshing(false);
                ApiResponse<DashboardData> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    bind(body.data);
                } else {
                    ApiUtils.toast(StudentDashboardActivity.this,
                            (body != null && body.message != null) ? body.message : getString(R.string.network_error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DashboardData>> call, Throwable t) {
                b.swipeRefresh.setRefreshing(false);
                ApiUtils.toast(StudentDashboardActivity.this, getString(R.string.network_error));
            }
        });
    }

    private void bind(DashboardData d) {
        b.welcomeText.setText(getString(R.string.welcome_prefix) + " " + d.name);

        boolean hasProject = d.project != null;
        boolean hasGroup = d.group != null;

        // Stats cards reflect the current project when present.
        if (hasProject) {
            int p = d.project.progressPercent;
            b.progressValue.setText(formatPercent(p));
            b.progressBar.setProgress(p);
            b.projectProgressValue.setText(formatPercent(p));
            b.projectProgressBar.setProgress(p);
            b.teamName.setText(hasGroup ? d.group.name : "-");
            b.teamMembersCount.setText(getString(R.string.active_members_fmt,
                    hasGroup && d.group.members != null ? d.group.members.size() : 0));
        } else {
            b.progressValue.setText(formatPercent(0));
            b.progressBar.setProgress(0);
            b.projectProgressValue.setText(formatPercent(0));
            b.projectProgressBar.setProgress(0);
            b.teamName.setText(hasGroup ? d.group.name : "-");
            b.teamMembersCount.setText(getString(R.string.active_members_fmt,
                    hasGroup && d.group.members != null ? d.group.members.size() : 0));
        }

        // Project card.
        if (hasProject) {
            b.projectCard.setVisibility(View.VISIBLE);
            b.projectTitle.setText(d.project.title);
            b.projectDescription.setText(d.project.description);
            b.projectStatusChip.setText(StatusMapper.projectStatusLabel(d.project.status));
            b.projectStatusChip.setBackgroundResource(StatusMapper.projectStatusChipBg(d.project.status));
            b.projectStatusChip.setTextColor(ContextCompat.getColor(this,
                    StatusMapper.projectStatusTextColor(d.project.status)));

            final int projectId = d.project.id;
            b.btnViewTasks.setOnClickListener(v -> openProject(TasksActivity.class, projectId));
            b.btnViewReports.setOnClickListener(v -> openProject(ReportsActivity.class, projectId));
        } else {
            b.projectCard.setVisibility(View.GONE);
        }

        // Team card (Stitch: shown when the student belongs to a group).
        if (hasGroup) {
            b.teamCard.setVisibility(View.VISIBLE);
            b.teamCardTitle.setText(getString(R.string.your_team_fmt, d.group.name));
            populateMembers(d.group.members);
        } else {
            b.teamCard.setVisibility(View.GONE);
        }

        // Evaluation card.
        if (hasProject && d.project.evaluation != null) {
            b.evaluationCard.setVisibility(View.VISIBLE);
            b.scoreChip.setText(formatScore(d.project.evaluation.score));
            String prof = d.project.professorName;
            b.evalSupervisorLabel.setText((prof == null || prof.isEmpty())
                    ? getString(R.string.final_evaluation)
                    : getString(R.string.supervisor_comment_fmt, prof));
            b.evalComment.setText(d.project.evaluation.comments == null ? "" : d.project.evaluation.comments);
        } else {
            b.evaluationCard.setVisibility(View.GONE);
        }

        // Empty states.
        b.emptyNoTeam.setVisibility(!hasGroup ? View.VISIBLE : View.GONE);
        b.emptyNoProject.setVisibility(hasGroup && !hasProject ? View.VISIBLE : View.GONE);
    }

    /** Builds the team member rows dynamically inside the team card. */
    private void populateMembers(java.util.List<DashboardData.Member> members) {
        b.teamMembersContainer.removeAllViews();
        if (members == null) return;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < members.size(); i++) {
            DashboardData.Member m = members.get(i);
            ItemTeamMemberBinding row = ItemTeamMemberBinding.inflate(inflater, b.teamMembersContainer, false);
            row.memberName.setText(m.name);
            row.memberAvatar.setText(initials(m.name));
            row.memberRole.setText(i == 0
                    ? getString(R.string.team_leader)
                    : getString(R.string.team_member));
            row.memberAvatar.setBackgroundResource(
                    i == 0 ? R.drawable.bg_circle_primary : R.drawable.bg_circle_secondary);
            b.teamMembersContainer.addView(row.getRoot());
        }
    }

    /** Persian-friendly initials: first letters of the first and last name words. */
    private String initials(String name) {
        if (name == null) return "";
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return "";
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 1) {
            String w = parts[0];
            return w.length() >= 2 ? w.substring(0, 2) : w;
        }
        return "" + parts[0].charAt(0) + "\u200c" + parts[parts.length - 1].charAt(0);
    }

    private void openProject(Class<?> target, int projectId) {
        Intent i = new Intent(this, target);
        i.putExtra("project_id", projectId);
        startActivity(i);
    }

    private String formatPercent(int value) {
        return toPersianDigits(value) + "٪";
    }

    private String formatScore(double score) {
        long rounded = Math.round(score);
        return toPersianDigits((int) rounded) + " / ۲۰";
    }

    /** Converts ASCII digits to Persian digits for UI consistency. */
    private String toPersianDigits(String in) {
        StringBuilder sb = new StringBuilder();
        for (char c : in.toCharArray()) {
            if (c >= '0' && c <= '9') {
                sb.append((char) ('۰' + (c - '0')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String toPersianDigits(int in) {
        return toPersianDigits(Integer.toString(in));
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
