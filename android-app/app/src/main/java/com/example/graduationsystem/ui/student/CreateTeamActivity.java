package com.example.graduationsystem.ui.student;

import android.os.Bundle;

import com.example.graduationsystem.ui.BaseActivity;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.DashboardData;
import com.example.graduationsystem.databinding.ActivityCreateTeamBinding;
import com.example.graduationsystem.util.ApiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Creates a team (group) with comma-separated member emails. Student only. */
public class CreateTeamActivity extends BaseActivity {

    private ActivityCreateTeamBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCreateTeamBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.btnBack.setOnClickListener(v -> finish());
        b.btnCancel.setOnClickListener(v -> finish());
        b.btnSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        String name = b.inputTeamName.getText().toString().trim();
        String emails = b.inputMemberEmails.getText().toString().trim();
        if (name.isEmpty()) {
            ApiUtils.toast(this, getString(R.string.label_team_name));
            return;
        }

        setLoading(true);
        ApiClient.get(this).createGroup(name, emails).enqueue(new Callback<ApiResponse<DashboardData.Group>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardData.Group>> call, Response<ApiResponse<DashboardData.Group>> response) {
                setLoading(false);
                ApiResponse<DashboardData.Group> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    ApiUtils.toast(CreateTeamActivity.this, getString(R.string.create_new_team));
                    finish();
                } else {
                    ApiUtils.toast(CreateTeamActivity.this,
                            (body != null && body.message != null) ? body.message : getString(R.string.generic_error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DashboardData.Group>> call, Throwable t) {
                setLoading(false);
                ApiUtils.toast(CreateTeamActivity.this, getString(R.string.network_error));
            }
        });
    }

    private void setLoading(boolean loading) {
        b.btnSubmit.setEnabled(!loading);
        b.btnSubmit.setText(loading ? R.string.loading : R.string.create_new_team);
    }
}
