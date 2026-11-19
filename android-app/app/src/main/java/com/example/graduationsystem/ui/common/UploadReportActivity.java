package com.example.graduationsystem.ui.common;

import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.example.graduationsystem.ui.BaseActivity;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.ReportsPayload;
import com.example.graduationsystem.databinding.ActivityUploadReportBinding;
import com.example.graduationsystem.util.ApiUtils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Student uploads a report file (with optional linked task) via multipart request. */
public class UploadReportActivity extends BaseActivity {

    private ActivityUploadReportBinding b;
    private int projectId;
    private Uri selectedUri;
    private String selectedName;
    private final List<ReportsPayload.TaskRef> tasks = new ArrayList<>();

    private final ActivityResultLauncher<String> picker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedUri = uri;
                    selectedName = queryName(uri);
                    b.fileNameText.setText(selectedName);
                    b.fileInfoRow.setVisibility(android.view.View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityUploadReportBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        projectId = getIntent().getIntExtra(TasksActivity.EXTRA_PROJECT_ID, 0);
        b.btnBack.setOnClickListener(v -> finish());
        b.btnCancel.setOnClickListener(v -> finish());
        android.view.View.OnClickListener openPicker = v -> picker.launch("*/*");
        b.fileField.setOnClickListener(openPicker);
        b.btnBrowse.setOnClickListener(openPicker);
        b.btnRemoveFile.setOnClickListener(v -> {
            selectedUri = null;
            selectedName = null;
            b.fileNameText.setText("");
            b.fileInfoRow.setVisibility(android.view.View.GONE);
        });
        b.btnSubmit.setOnClickListener(v -> submit());
        loadTasks();
    }

    private void loadTasks() {
        ApiClient.get(this).reports(projectId).enqueue(new Callback<ApiResponse<ReportsPayload>>() {
            @Override
            public void onResponse(Call<ApiResponse<ReportsPayload>> call, Response<ApiResponse<ReportsPayload>> response) {
                ApiResponse<ReportsPayload> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    tasks.clear();
                    if (body.data.tasks != null) tasks.addAll(body.data.tasks);
                    List<String> labels = new ArrayList<>();
                    labels.add(getString(R.string.none_option));
                    for (ReportsPayload.TaskRef t : tasks) labels.add(t.title);
                    b.dropdownTask.setAdapter(new ArrayAdapter<>(UploadReportActivity.this,
                            android.R.layout.simple_list_item_1, labels));
                    b.dropdownTask.setText(labels.get(0), false);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ReportsPayload>> call, Throwable t) {
                // Task list is optional; ignore failures and still allow upload without link.
            }
        });
    }

    private void submit() {
        if (selectedUri == null) {
            ApiUtils.toast(this, getString(R.string.pick_file));
            return;
        }

        String mime = getContentResolver().getType(selectedUri);
        MediaType mediaType = MediaType.parse(mime == null ? "application/octet-stream" : mime);
        MultipartBody.Part part;
        try (java.io.InputStream in = getContentResolver().openInputStream(selectedUri)) {
            byte[] bytes = readAll(in);
            RequestBody fileBody = RequestBody.create(mediaType, bytes);
            part = MultipartBody.Part.createFormData("file",
                    selectedName == null ? "report" : selectedName, fileBody);
        } catch (Exception e) {
            ApiUtils.toast(this, getString(R.string.generic_error));
            return;
        }

        Integer taskId = resolveTaskId();
        RequestBody taskIdBody = taskId == null ? null
                : RequestBody.create(okhttp3.MultipartBody.FORM, String.valueOf(taskId));

        setLoading(true);
        ApiClient.get(this).uploadReport(projectId, part, taskIdBody)
                .enqueue(new Callback<ApiResponse<ReportsPayload.ReportItem>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ReportsPayload.ReportItem>> call, Response<ApiResponse<ReportsPayload.ReportItem>> response) {
                        setLoading(false);
                        ApiResponse<ReportsPayload.ReportItem> body = response.body();
                        if (response.isSuccessful() && body != null && body.data != null) {
                            ApiUtils.toast(UploadReportActivity.this, getString(R.string.upload_report));
                            finish();
                        } else {
                            ApiUtils.toast(UploadReportActivity.this,
                                    (body != null && body.message != null) ? body.message : getString(R.string.generic_error));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReportsPayload.ReportItem>> call, Throwable t) {
                        setLoading(false);
                        ApiUtils.toast(UploadReportActivity.this, getString(R.string.network_error));
                    }
                });
    }

    private Integer resolveTaskId() {
        CharSequence text = b.dropdownTask.getText();
        if (text == null) return null;
        String label = text.toString();
        if (label.isEmpty() || label.equals(getString(R.string.none_option))) return null;
        for (ReportsPayload.TaskRef t : tasks) {
            if (t.title.equals(label)) return t.id;
        }
        return null;
    }

    private String queryName(Uri uri) {
        try (android.database.Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) {
        }
        return uri.getLastPathSegment() == null ? "report" : uri.getLastPathSegment();
    }

    private byte[] readAll(java.io.InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
        return out.toByteArray();
    }

    private void setLoading(boolean loading) {
        b.btnSubmit.setEnabled(!loading);
        b.btnSubmit.setText(loading ? R.string.loading : R.string.upload_report);
    }
}
