package com.example.graduationsystem.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.graduationsystem.ui.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.AdminProject;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.databinding.ActivityAdminProjectsBinding;
import com.example.graduationsystem.ui.professor.EvaluationActivity;
import com.example.graduationsystem.util.ApiUtils;
import com.example.graduationsystem.util.FormDialog;
import com.example.graduationsystem.util.StatusMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Admin lists all projects, filters by status chip + text, and manages (create/edit/delete) them. */
public class AdminProjectsActivity extends BaseActivity implements AdminProjectAdapter.Listener {

    private static final String MODE_ALL = "all";
    private static final String MODE_IN_PROGRESS = "in_progress";
    private static final String MODE_NEEDS_EVAL = "needs_eval";
    private static final String MODE_COMPLETED = "completed";

    /** Backend status keys in the order shown in the picker dropdown. */
    private static final List<String> STATUS_KEYS = Arrays.asList(
            "pending", "approved", "rejected", "in_progress", "submitted", "evaluated");

    private ActivityAdminProjectsBinding b;
    private AdminProjectAdapter adapter;
    private final List<AdminProject> all = new ArrayList<>();
    private String filterMode = MODE_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAdminProjectsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        adapter = new AdminProjectAdapter(this);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        b.btnBack.setOnClickListener(v -> finish());
        b.btnNewProject.setOnClickListener(v -> showCreateProjectDialog());
        b.btnTune.setOnClickListener(v ->
                ApiUtils.toast(this, getString(R.string.filters)));
        b.swipeRefresh.setOnRefreshListener(this::load);

        setupChip(b.chipAll, b.chipAllLabel, b.chipAllCount, MODE_ALL);
        setupChip(b.chipInProgress, b.chipInProgressLabel, b.chipInProgressCount, MODE_IN_PROGRESS);
        setupChip(b.chipNeedsEval, b.chipNeedsEvalLabel, b.chipNeedsEvalCount, MODE_NEEDS_EVAL);
        setupChip(b.chipCompleted, b.chipCompletedLabel, b.chipCompletedCount, MODE_COMPLETED);

        b.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        load();
    }

    private void setupChip(LinearLayout root, TextView label, TextView count, String mode) {
        root.setOnClickListener(v -> {
            filterMode = mode;
            updateChipStates();
            applyFilter();
        });
    }

    private void updateChipStates() {
        styleChip(b.chipAll, b.chipAllLabel, b.chipAllCount, MODE_ALL.equals(filterMode));
        styleChip(b.chipInProgress, b.chipInProgressLabel, b.chipInProgressCount, MODE_IN_PROGRESS.equals(filterMode));
        styleChip(b.chipNeedsEval, b.chipNeedsEvalLabel, b.chipNeedsEvalCount, MODE_NEEDS_EVAL.equals(filterMode));
        styleChip(b.chipCompleted, b.chipCompletedLabel, b.chipCompletedCount, MODE_COMPLETED.equals(filterMode));
    }

    private void styleChip(LinearLayout root, TextView label, TextView count, boolean selected) {
        root.setBackgroundResource(selected ? R.drawable.bg_filter_chip_sel : R.drawable.bg_filter_chip_unsel);
        int fg = selected ? R.color.on_primary_container : R.color.on_surface_variant;
        label.setTextColor(ContextCompat.getColor(this, fg));
        count.setBackgroundResource(selected ? R.drawable.bg_chip_count_sel : countBgFor(count));
        count.setTextColor(ContextCompat.getColor(this, fg));
    }

    private int countBgFor(TextView count) {
        return count.getId() == R.id.chipNeedsEvalCount
                ? R.drawable.bg_chip_count_error : R.drawable.bg_chip_count_unsel;
    }

    private void load() {
        b.loading.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
        ApiClient.get(this).adminProjects().enqueue(new Callback<ApiResponse<List<AdminProject>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AdminProject>>> call, Response<ApiResponse<List<AdminProject>>> response) {
                b.swipeRefresh.setRefreshing(false);
                b.loading.setVisibility(View.GONE);
                ApiResponse<List<AdminProject>> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    all.clear();
                    all.addAll(body.data);
                    updateCounts();
                    applyFilter();
                } else {
                    ApiUtils.toast(AdminProjectsActivity.this,
                            (body != null && body.message != null) ? body.message : getString(R.string.network_error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AdminProject>>> call, Throwable t) {
                b.swipeRefresh.setRefreshing(false);
                b.loading.setVisibility(View.GONE);
                ApiUtils.toast(AdminProjectsActivity.this, getString(R.string.network_error));
            }
        });
    }

    private void updateCounts() {
        int inProgress = 0, needsEval = 0, completed = 0;
        for (AdminProject p : all) {
            if (matchesMode(p, MODE_IN_PROGRESS)) inProgress++;
            else if (matchesMode(p, MODE_NEEDS_EVAL)) needsEval++;
            else if (matchesMode(p, MODE_COMPLETED)) completed++;
        }
        b.chipAllCount.setText(faDigits(Integer.toString(all.size())));
        b.chipInProgressCount.setText(faDigits(Integer.toString(inProgress)));
        b.chipNeedsEvalCount.setText(faDigits(Integer.toString(needsEval)));
        b.chipCompletedCount.setText(faDigits(Integer.toString(completed)));
    }

    private boolean matchesMode(AdminProject p, String mode) {
        String s = p.status == null ? "" : p.status;
        switch (mode) {
            case MODE_IN_PROGRESS:
                return "in_progress".equals(s) || "approved".equals(s);
            case MODE_NEEDS_EVAL:
                return "submitted".equals(s);
            case MODE_COMPLETED:
                return "evaluated".equals(s);
            case MODE_ALL:
            default:
                return true;
        }
    }

    private void applyFilter() {
        String q = b.inputSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<AdminProject> filtered = new ArrayList<>();
        for (AdminProject p : all) {
            if (!MODE_ALL.equals(filterMode) && !matchesMode(p, filterMode)) continue;
            if (!q.isEmpty()) {
                boolean match = (p.title != null && p.title.toLowerCase(Locale.ROOT).contains(q))
                        || (p.groupName != null && p.groupName.toLowerCase(Locale.ROOT).contains(q))
                        || (p.professorName != null && p.professorName.toLowerCase(Locale.ROOT).contains(q));
                if (!match) continue;
            }
            filtered.add(p);
        }
        adapter.submit(filtered);
    }

    // ---------------- CRUD dialogs ----------------

    private List<String> statusLabels() {
        List<String> labels = new ArrayList<>();
        for (String k : STATUS_KEYS) labels.add(getString(StatusMapper.projectStatusLabel(k)));
        return labels;
    }

    private String statusKeyAt(int index) {
        if (index < 0 || index >= STATUS_KEYS.size()) return "pending";
        return STATUS_KEYS.get(index);
    }

    private int statusIndexOf(String key) {
        int i = STATUS_KEYS.indexOf(key == null ? "pending" : key);
        return i < 0 ? 0 : i;
    }

    private void showCreateProjectDialog() {
        FormDialog form = FormDialog.build(this)
                .text("title", getString(R.string.label_project_title), null, false, false)
                .text("description", getString(R.string.label_project_desc), null, true, false)
                .dropdown("status", getString(R.string.label_status), getString(R.string.status_pending), statusLabels())
                .text("start_date", getString(R.string.label_start_date), null, false, false)
                .text("end_date", getString(R.string.label_end_date), null, false, false);

        new AlertDialog.Builder(this)
                .setTitle(R.string.create_project_title)
                .setView(form.getRoot())
                .setPositiveButton(R.string.save, (d, w) -> {
                    String title = form.value("title");
                    if (title == null) {
                        ApiUtils.toast(this, getString(R.string.field_required_title));
                        return;
                    }
                    String desc = form.value("description");
                    String startDate = normalizeDate(form.value("start_date"));
                    String endDate = normalizeDate(form.value("end_date"));
                    String status = resolveStatus(form.value("status"));
                    ApiClient.get(this).createAdminProject(title, desc, null, startDate, endDate, status)
                            .enqueue(new Callback<ApiResponse<AdminProject>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<AdminProject>> call, Response<ApiResponse<AdminProject>> r) {
                                    if (r.isSuccessful() && r.body() != null && r.body().data != null) {
                                        ApiUtils.toast(AdminProjectsActivity.this, getString(R.string.action_saved_toast));
                                        load();
                                    } else {
                                        ApiUtils.toast(AdminProjectsActivity.this, message(r.body()));
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<AdminProject>> call, Throwable t) {
                                    ApiUtils.toast(AdminProjectsActivity.this, getString(R.string.network_error));
                                }
                            });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onManage(AdminProject project) {
        FormDialog form = FormDialog.build(this)
                .text("title", getString(R.string.label_project_title), project.title, false, false)
                .text("description", getString(R.string.label_project_desc), project.description, true, false)
                .dropdown("status", getString(R.string.label_status),
                        getString(StatusMapper.projectStatusLabel(project.status)), statusLabels())
                .text("start_date", getString(R.string.label_start_date), project.startDate, false, false)
                .text("end_date", getString(R.string.label_end_date), project.endDate, false, false)
                .text("progress_percent", getString(R.string.label_progress),
                        Integer.toString(project.progressPercent), false, false);

        new AlertDialog.Builder(this)
                .setTitle(R.string.manage_project_title)
                .setView(form.getRoot())
                .setPositiveButton(R.string.save, (d, w) -> {
                    String title = form.value("title");
                    if (title == null) {
                        ApiUtils.toast(this, getString(R.string.field_required_title));
                        return;
                    }
                    String desc = form.value("description");
                    String startDate = normalizeDate(form.value("start_date"));
                    String endDate = normalizeDate(form.value("end_date"));
                    String status = resolveStatus(form.value("status"));
                    Integer progress = parseIntOrNull(form.value("progress_percent"));
                    ApiClient.get(this).updateAdminProject(project.id, title, desc, project.professorId,
                                    startDate, endDate, status, progress)
                            .enqueue(new Callback<ApiResponse<AdminProject>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<AdminProject>> call, Response<ApiResponse<AdminProject>> r) {
                                    if (r.isSuccessful() && r.body() != null && r.body().data != null) {
                                        ApiUtils.toast(AdminProjectsActivity.this, getString(R.string.action_saved_toast));
                                        load();
                                    } else {
                                        ApiUtils.toast(AdminProjectsActivity.this, message(r.body()));
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<AdminProject>> call, Throwable t) {
                                    ApiUtils.toast(AdminProjectsActivity.this, getString(R.string.network_error));
                                }
                            });
                })
                .setNeutralButton(R.string.delete, (d, w) -> confirmDelete(project))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(AdminProject project) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_project_fmt, project.title))
                .setPositiveButton(R.string.delete, (d, w) ->
                        ApiClient.get(this).deleteAdminProject(project.id).enqueue(new Callback<ApiResponse<Object>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> r) {
                                if (r.isSuccessful()) {
                                    ApiUtils.toast(AdminProjectsActivity.this, getString(R.string.action_deleted_toast));
                                    load();
                                } else {
                                    ApiUtils.toast(AdminProjectsActivity.this, getString(R.string.action_failed_toast));
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                ApiUtils.toast(AdminProjectsActivity.this, getString(R.string.network_error));
                            }
                        }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onEvaluate(AdminProject project) {
        Intent i = new Intent(this, EvaluationActivity.class);
        i.putExtra("project_id", project.id);
        i.putExtra("project_title", project.title);
        startActivity(i);
    }

    @Override
    public void onExtendDeadline(AdminProject project) {
        FormDialog form = FormDialog.build(this)
                .text("end_date", getString(R.string.extend_deadline_hint),
                        project.endDate, false, false);

        new AlertDialog.Builder(this)
                .setTitle(R.string.extend_deadline_title)
                .setView(form.getRoot())
                .setPositiveButton(R.string.save, (d, w) -> {
                    String endDate = normalizeDate(form.value("end_date"));
                    if (endDate == null || endDate.isEmpty()) {
                        ApiUtils.toast(this, getString(R.string.field_required_title));
                        return;
                    }
                    ApiClient.get(this).extendDeadline(project.id, endDate)
                            .enqueue(new Callback<ApiResponse<AdminProject>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<AdminProject>> call,
                                                       Response<ApiResponse<AdminProject>> r) {
                                    if (r.isSuccessful() && r.body() != null && r.body().data != null) {
                                        ApiUtils.toast(AdminProjectsActivity.this,
                                                getString(R.string.deadline_extended_fmt, endDate));
                                        load();
                                    } else {
                                        ApiUtils.toast(AdminProjectsActivity.this, message(r.body()));
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<AdminProject>> call, Throwable t) {
                                    ApiUtils.toast(AdminProjectsActivity.this, getString(R.string.network_error));
                                }
                            });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ---------------- helpers ----------------

    /** Maps a Persian status label back to its backend key; defaults to pending. */
    private String resolveStatus(String label) {
        if (label == null) return "pending";
        List<String> labels = statusLabels();
        int idx = labels.indexOf(label);
        return statusKeyAt(idx);
    }

    /** Accepts yyyy-MM-dd or yyyy/MM/dd and normalizes to yyyy-MM-dd; null when blank. */
    private static String normalizeDate(String in) {
        if (in == null) return null;
        String s = in.trim().replace('/', '-');
        return s.isEmpty() ? null : s;
    }

    private static Integer parseIntOrNull(String in) {
        if (in == null) return null;
        try {
            return Integer.parseInt(in.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String message(ApiResponse<?> body) {
        return (body != null && body.message != null) ? body.message : getString(R.string.action_failed_toast);
    }

    private String faDigits(String in) {
        StringBuilder sb = new StringBuilder();
        for (char c : in.toCharArray()) {
            sb.append(c >= '0' && c <= '9' ? (char) ('\u06F0' + (c - '0')) : c);
        }
        return sb.toString();
    }
}
