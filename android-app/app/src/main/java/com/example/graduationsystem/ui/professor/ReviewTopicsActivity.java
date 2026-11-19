package com.example.graduationsystem.ui.professor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import com.example.graduationsystem.ui.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.ProfessorProject;
import com.example.graduationsystem.databinding.ActivityReviewTopicsBinding;
import com.example.graduationsystem.util.ApiUtils;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Professor reviews topics: pending ones get inline start/end dates then approve or reject. */
public class ReviewTopicsActivity extends BaseActivity implements TopicAdapter.Listener {

    private ActivityReviewTopicsBinding b;
    private TopicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityReviewTopicsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        adapter = new TopicAdapter(this);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        b.btnBack.setOnClickListener(v -> finish());
        b.swipeRefresh.setOnRefreshListener(this::load);
        load();
    }

    private void load() {
        b.swipeRefresh.setRefreshing(true);
        ApiClient.get(this).professorProjects().enqueue(new Callback<ApiResponse<java.util.List<ProfessorProject>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.List<ProfessorProject>>> call,
                                   Response<ApiResponse<java.util.List<ProfessorProject>>> response) {
                b.swipeRefresh.setRefreshing(false);
                ApiResponse<java.util.List<ProfessorProject>> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    adapter.submit(body.data);
                    int pending = 0;
                    for (ProfessorProject p : body.data) {
                        if ("pending".equals(p.status)) pending++;
                    }
                    // Only show empty overlay when there are truly NO projects at all
                    b.emptyText.setVisibility(body.data.isEmpty() ? View.VISIBLE : View.GONE);
                    // Stitch subtitle: "۳ موضوع در انتظار بررسی شماست"
                    if (pending > 0) {
                        b.subtitleText.setText(getString(R.string.pending_topics_subtitle_fmt, toFaDigits(pending)));
                        b.subtitleText.setVisibility(View.VISIBLE);
                    } else {
                        b.subtitleText.setVisibility(View.GONE);
                    }
                } else {
                    ApiUtils.toast(ReviewTopicsActivity.this,
                            (body != null && body.message != null) ? body.message : getString(R.string.network_error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.List<ProfessorProject>>> call, Throwable t) {
                b.swipeRefresh.setRefreshing(false);
                ApiUtils.toast(ReviewTopicsActivity.this, getString(R.string.network_error));
            }
        });
    }

    @Override
    public void onPickStartDate(ProfessorProject project) {
        pickDate(value -> {
            adapter.selectionsFor(project.id).start = value;
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onPickEndDate(ProfessorProject project) {
        pickDate(value -> {
            adapter.selectionsFor(project.id).end = value;
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onApprove(ProfessorProject project) {
        TopicAdapter.DateSelections sel = adapter.selectionsFor(project.id);
        if (sel.start == null || sel.end == null) {
            ApiUtils.toast(this, getString(R.string.pick_start_end));
            return;
        }
        ApiClient.get(this).approveProject(project.id, sel.start, sel.end)
                .enqueue(simpleCallback(project.id));
    }

    @Override
    public void onReject(ProfessorProject project) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reject)
                .setMessage(project.title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.reject, (d, w) ->
                        ApiClient.get(this).rejectProject(project.id).enqueue(simpleCallback(project.id)))
                .show();
    }

    private interface DatePicked {
        void onPick(String value);
    }

    /** Renders a number with Persian digits to match the Stitch design. */
    private static String toFaDigits(int n) {
        char[] d = String.valueOf(n).toCharArray();
        StringBuilder sb = new StringBuilder(d.length);
        for (char c : d) {
            sb.append(c >= '0' && c <= '9' ? (char) ('\u06F0' + (c - '0')) : c);
        }
        return sb.toString();
    }

    private void pickDate(DatePicked cb) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                cb.onPick(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private Callback<ApiResponse<Object>> simpleCallback(int projectId) {
        return new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                ApiResponse<Object> body = response.body();
                if (response.isSuccessful()) {
                    adapter.clearSelections(projectId);
                    ApiUtils.toast(ReviewTopicsActivity.this, getString(R.string.save));
                    load();
                } else {
                    ApiUtils.toast(ReviewTopicsActivity.this,
                            (body != null && body.message != null) ? body.message : getString(R.string.generic_error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                ApiUtils.toast(ReviewTopicsActivity.this, getString(R.string.network_error));
            }
        };
    }
}
