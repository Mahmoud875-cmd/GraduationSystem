package com.example.graduationsystem.ui.common;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.graduationsystem.ui.BaseActivity;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.DashboardData;
import com.example.graduationsystem.data.model.TasksPayload;
import com.example.graduationsystem.databinding.ActivityAddTaskBinding;
import com.example.graduationsystem.util.ApiUtils;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Professor creates a task, assigning it to a team member with a deadline. */
public class AddTaskActivity extends BaseActivity {

    private ActivityAddTaskBinding b;
    private int projectId;
    private final List<DashboardData.Member> members = new ArrayList<>();
    private String deadlineValue = "";

    // Priority selector is decorative: the API has no priority field.
    private LinearLayout prioUrgent, prioNormal, prioLow;
    private TextView textUrgent, textNormal, textLow;
    private android.view.View dotUrgent, dotNormal, dotLow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        projectId = getIntent().getIntExtra(TasksActivity.EXTRA_PROJECT_ID, 0);
        b.btnBack.setOnClickListener(v -> finish());
        b.btnCancel.setOnClickListener(v -> finish());
        b.deadlineField.setOnClickListener(v -> pickDate());
        b.btnSubmit.setOnClickListener(v -> submit());

        prioUrgent = b.prioUrgent;
        prioNormal = b.prioNormal;
        prioLow = b.prioLow;
        textUrgent = b.textUrgent;
        textNormal = b.textNormal;
        textLow = b.textLow;
        dotUrgent = b.dotUrgent;
        dotNormal = b.dotNormal;
        dotLow = b.dotLow;
        prioUrgent.setOnClickListener(v -> selectPriority(0));
        prioNormal.setOnClickListener(v -> selectPriority(1));
        prioLow.setOnClickListener(v -> selectPriority(2));
        selectPriority(1);

        loadTeam();
    }

    /** Highlight one of the three priority chips (normal is the default). */
    private void selectPriority(int which) {
        LinearLayout[] chips = {prioUrgent, prioNormal, prioLow};
        TextView[] texts = {textUrgent, textNormal, textLow};
        android.view.View[] dots = {dotUrgent, dotNormal, dotLow};
        int[] dotColors = {
                ContextCompat.getColor(this, R.color.error),
                ContextCompat.getColor(this, R.color.surface),
                ContextCompat.getColor(this, R.color.outline)};
        for (int i = 0; i < chips.length; i++) {
            boolean active = i == which;
            chips[i].setBackgroundResource(active
                    ? R.drawable.bg_chip_priority_selected : R.drawable.bg_chip_priority_unselected);
            texts[i].setTextColor(active
                    ? ContextCompat.getColor(this, R.color.on_primary)
                    : ContextCompat.getColor(this, R.color.on_surface_variant));
            dots[i].setBackgroundTintList(ColorStateList.valueOf(dotColors[i]));
        }
    }

    private void loadTeam() {
        ApiClient.get(this).tasks(projectId).enqueue(new Callback<ApiResponse<TasksPayload>>() {
            @Override
            public void onResponse(Call<ApiResponse<TasksPayload>> call, Response<ApiResponse<TasksPayload>> response) {
                ApiResponse<TasksPayload> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null && body.data.teamMembers != null) {
                    members.clear();
                    members.addAll(body.data.teamMembers);
                    List<String> names = new ArrayList<>();
                    for (DashboardData.Member m : members) names.add(m.name);
                    b.dropdownAssignee.setAdapter(new ArrayAdapter<>(AddTaskActivity.this,
                            android.R.layout.simple_list_item_1, names));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TasksPayload>> call, Throwable t) {
                ApiUtils.toast(AddTaskActivity.this, getString(R.string.network_error));
            }
        });
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            deadlineValue = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            b.deadlineText.setText(deadlineValue);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void submit() {
        String title = b.inputTitle.getText().toString().trim();
        String description = b.inputDescription.getText().toString().trim();
        if (title.isEmpty()) {
            ApiUtils.toast(this, getString(R.string.label_task_title));
            return;
        }
        if (description.isEmpty()) {
            ApiUtils.toast(this, getString(R.string.label_task_desc));
            return;
        }
        if (deadlineValue.isEmpty()) {
            ApiUtils.toast(this, getString(R.string.label_deadline));
            return;
        }
        int assigneeId = resolveAssigneeId();
        if (assigneeId == 0) {
            ApiUtils.toast(this, getString(R.string.label_assignee));
            return;
        }

        setLoading(true);
        ApiClient.get(this).createTask(projectId, title, description, deadlineValue, assigneeId)
                .enqueue(new Callback<ApiResponse<TasksPayload.TaskItem>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<TasksPayload.TaskItem>> call, Response<ApiResponse<TasksPayload.TaskItem>> response) {
                        setLoading(false);
                        ApiResponse<TasksPayload.TaskItem> body = response.body();
                        if (response.isSuccessful() && body != null && body.data != null) {
                            ApiUtils.toast(AddTaskActivity.this, getString(R.string.task_created));
                            finish();
                        } else {
                            ApiUtils.toast(AddTaskActivity.this,
                                    (body != null && body.message != null) ? body.message : getString(R.string.generic_error));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<TasksPayload.TaskItem>> call, Throwable t) {
                        setLoading(false);
                        ApiUtils.toast(AddTaskActivity.this, getString(R.string.network_error));
                    }
                });
    }

    private int resolveAssigneeId() {
        CharSequence text = b.dropdownAssignee.getText();
        if (text == null) return 0;
        String label = text.toString();
        for (DashboardData.Member m : members) {
            if (m.name.equals(label)) return m.id;
        }
        return 0;
    }

    private void setLoading(boolean loading) {
        b.btnSubmit.setEnabled(!loading);
        b.btnSubmit.setText(loading ? R.string.loading : R.string.add_task);
    }
}
