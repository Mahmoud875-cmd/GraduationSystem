package com.example.graduationsystem.ui.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.example.graduationsystem.ui.BaseActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.ReportsPayload;
import com.example.graduationsystem.databinding.ActivityReportsBinding;
import com.example.graduationsystem.util.ApiUtils;
import com.example.graduationsystem.util.Session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Lists project reports. Students upload/download; professors add feedback and download. */
public class ReportsActivity extends BaseActivity implements ReportAdapter.Listener {

    private ActivityReportsBinding b;
    private ReportAdapter adapter;
    private int projectId;
    private boolean isProfessor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        projectId = getIntent().getIntExtra(TasksActivity.EXTRA_PROJECT_ID, 0);
        adapter = new ReportAdapter(false, "", this);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        b.btnBack.setOnClickListener(v -> finish());
        b.swipeRefresh.setOnRefreshListener(this::load);
        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (projectId != 0) load();
    }

    private void load() {
        b.swipeRefresh.setRefreshing(true);
        ApiClient.get(this).reports(projectId).enqueue(new Callback<ApiResponse<ReportsPayload>>() {
            @Override
            public void onResponse(Call<ApiResponse<ReportsPayload>> call, Response<ApiResponse<ReportsPayload>> response) {
                b.swipeRefresh.setRefreshing(false);
                ApiResponse<ReportsPayload> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    bind(body.data);
                } else {
                    ApiUtils.toast(ReportsActivity.this,
                            (body != null && body.message != null) ? body.message : getString(R.string.network_error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ReportsPayload>> call, Throwable t) {
                b.swipeRefresh.setRefreshing(false);
                ApiUtils.toast(ReportsActivity.this, getString(R.string.network_error));
            }
        });
    }

    private void bind(ReportsPayload data) {
        b.projectTitle.setText(data.project.title);
        isProfessor = data.project.isProfessor;

        // Rebuild adapter so professor-only inline comment editors appear correctly.
        String supervisor = Session.get(this).getName();
        adapter = new ReportAdapter(isProfessor, supervisor, this);
        b.recycler.setAdapter(adapter);
        adapter.submit(data.reports);
        b.emptyText.setVisibility(data.reports == null || data.reports.isEmpty() ? View.VISIBLE : View.GONE);

        b.fabUpload.setVisibility(isProfessor ? View.GONE : View.VISIBLE);
        b.fabUpload.setOnClickListener(v -> {
            Intent i = new Intent(this, UploadReportActivity.class);
            i.putExtra(TasksActivity.EXTRA_PROJECT_ID, projectId);
            startActivity(i);
        });
    }

    @Override
    public void onDownload(ReportsPayload.ReportItem report) {
        if (report.downloadUrl == null || report.downloadUrl.isEmpty()) {
            ApiUtils.toast(this, getString(R.string.generic_error));
            return;
        }
        ApiUtils.toast(this, getString(R.string.loading));
        ApiClient.get(this).downloadFile(report.downloadUrl).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                        if (dir == null) dir = getFilesDir();
                        File out = new File(dir, report.fileName == null ? "report" : report.fileName);
                        try (InputStream in = response.body().byteStream();
                             FileOutputStream fos = new FileOutputStream(out)) {
                            byte[] buf = new byte[8192];
                            int len;
                            while ((len = in.read(buf)) != -1) fos.write(buf, 0, len);
                        }
                        Uri uri = FileProvider.getUriForFile(ReportsActivity.this,
                                getPackageName() + ".fileprovider", out);
                        Intent open = new Intent(Intent.ACTION_VIEW);
                        open.setDataAndType(uri, "application/octet-stream");
                        open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(open);
                    } catch (Exception e) {
                        ApiUtils.toast(ReportsActivity.this, getString(R.string.generic_error));
                    }
                } else {
                    ApiUtils.toast(ReportsActivity.this, getString(R.string.network_error));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ApiUtils.toast(ReportsActivity.this, getString(R.string.network_error));
            }
        });
    }

    @Override
    public void onFeedback(ReportsPayload.ReportItem report, String text) {
        sendFeedback(report.id, text);
    }

    private void sendFeedback(int reportId, String feedback) {
        ApiClient.get(this).reportFeedback(reportId, feedback).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                ApiResponse<Object> body = response.body();
                if (response.isSuccessful()) {
                    ApiUtils.toast(ReportsActivity.this, getString(R.string.save));
                    load();
                } else {
                    ApiUtils.toast(ReportsActivity.this,
                            (body != null && body.message != null) ? body.message : getString(R.string.generic_error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                ApiUtils.toast(ReportsActivity.this, getString(R.string.network_error));
            }
        });
    }
}
