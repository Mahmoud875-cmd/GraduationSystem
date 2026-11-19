package com.example.graduationsystem.ui.professor;

import android.os.Bundle;
import android.view.View;

import com.example.graduationsystem.ui.BaseActivity;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.EvaluationPayload;
import com.example.graduationsystem.databinding.ActivityEvaluationBinding;
import com.example.graduationsystem.util.ApiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Professor assigns a final score (0-20) and comments to a project. */
public class EvaluationActivity extends BaseActivity {

    private ActivityEvaluationBinding b;
    private int projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityEvaluationBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        projectId = getIntent().getIntExtra("project_id", 0);
        String title = getIntent().getStringExtra("project_title");
        b.projectTitle.setText(title == null ? "" : title);

        b.btnBack.setOnClickListener(v -> finish());
        b.btnSubmit.setOnClickListener(v -> submit());
        b.chipQuality.setOnClickListener(v -> appendFeedback(getString(R.string.eval_tag_quality_text)));
        b.chipDefense.setOnClickListener(v -> appendFeedback(getString(R.string.eval_tag_defense_text)));
        b.chipDocs.setOnClickListener(v -> appendFeedback(getString(R.string.eval_tag_docs_text)));
        loadInfo();
    }

    /** Appends a suggested phrase to the feedback textarea, matching the design's quick-feedback chips. */
    private void appendFeedback(String text) {
        android.widget.EditText input = b.inputComments;
        CharSequence cur = input.getText();
        String merged = (cur == null || cur.length() == 0) ? text : cur.toString().trim() + " " + text;
        input.setText(merged);
        input.setSelection(input.getText().length());
    }

    private void loadInfo() {
        ApiClient.get(this).evaluationInfo(projectId).enqueue(new Callback<ApiResponse<EvaluationPayload>>() {
            @Override
            public void onResponse(Call<ApiResponse<EvaluationPayload>> call, Response<ApiResponse<EvaluationPayload>> response) {
                ApiResponse<EvaluationPayload> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    EvaluationPayload p = body.data;
                    if (p.project != null) {
                        if (p.project.title != null) {
                            b.projectTitle.setText(p.project.title);
                        }
                        if (p.project.groupName != null && !p.project.groupName.isEmpty()) {
                            b.studentInfo.setText(getString(R.string.eval_group_fmt, p.project.groupName));
                            b.groupName.setText(p.project.groupName);
                        }
                    }
                    if (p.alreadyEvaluated && p.evaluation != null) {
                        b.alreadyBanner.setVisibility(View.VISIBLE);
                        b.inputScore.setText(String.valueOf(p.evaluation.score));
                        b.inputComments.setText(p.evaluation.comments);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<EvaluationPayload>> call, Throwable t) {
                // Info is optional; professor can still submit.
            }
        });
    }

    private void submit() {
        String scoreText = b.inputScore.getText().toString().trim();
        double score;
        try {
            score = Double.parseDouble(scoreText);
        } catch (NumberFormatException e) {
            ApiUtils.toast(this, getString(R.string.eval_grade_note));
            return;
        }
        if (score < 0 || score > 20) {
            ApiUtils.toast(this, getString(R.string.eval_grade_note));
            return;
        }
        String comments = b.inputComments.getText().toString().trim();

        setLoading(true);
        ApiClient.get(this).submitEvaluation(projectId, score, comments)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        setLoading(false);
                        ApiResponse<Object> body = response.body();
                        if (response.isSuccessful()) {
                            ApiUtils.toast(EvaluationActivity.this, getString(R.string.eval_saved_toast));
                            finish();
                        } else {
                            ApiUtils.toast(EvaluationActivity.this,
                                    (body != null && body.message != null) ? body.message : getString(R.string.generic_error));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        setLoading(false);
                        ApiUtils.toast(EvaluationActivity.this, getString(R.string.network_error));
                    }
                });
    }

    private void setLoading(boolean loading) {
        b.btnSubmit.setEnabled(!loading);
        b.btnSubmit.setText(loading ? R.string.loading : R.string.eval_save_btn);
    }
}
