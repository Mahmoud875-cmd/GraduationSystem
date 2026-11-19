package com.example.graduationsystem.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.graduationsystem.ui.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.api.ApiClient;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.TasksPayload;
import com.example.graduationsystem.databinding.ActivityTasksBinding;
import com.example.graduationsystem.util.ApiUtils;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Lists a project's tasks. Professors may change task status; students see read-only. */
public class TasksActivity extends BaseActivity implements TaskAdapter.OnStatusClick {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private ActivityTasksBinding b;
    private TaskAdapter adapter;
    private int projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityTasksBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        projectId = getIntent().getIntExtra(EXTRA_PROJECT_ID, 0);
        adapter = new TaskAdapter(true, this);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        b.btnBack.setOnClickListener(v -> finish());
        b.swipeRefresh.setOnRefreshListener(this::load);
        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (projectId != 0 && b.recycler.getAdapter() != null) {
            load();
        }
    }

    private void load() {
        b.swipeRefresh.setRefreshing(true);
        ApiClient.get(this).tasks(projectId).enqueue(new Callback<ApiResponse<TasksPayload>>() {
            @Override
            public void onResponse(Call<ApiResponse<TasksPayload>> call, Response<ApiResponse<TasksPayload>> response) {
                b.swipeRefresh.setRefreshing(false);
                ApiResponse<TasksPayload> body = response.body();
                if (response.isSuccessful() && body != null && body.data != null) {
                    bind(body.data);
                } else {
                    ApiUtils.toast(TasksActivity.this,
                            (body != null && body.message != null) ? body.message : getString(R.string.network_error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TasksPayload>> call, Throwable t) {
                b.swipeRefresh.setRefreshing(false);
                ApiUtils.toast(TasksActivity.this, getString(R.string.network_error));
            }
        });
    }

    private void bind(TasksPayload data) {
        b.projectTitle.setText(data.project.title);
        // Stitch progress card lives inside the list; skip it when there are no tasks.
        boolean empty = data.tasks == null || data.tasks.isEmpty();
        adapter.submit(data.tasks, empty ? 0 : data.project.progressPercent);

        boolean isProfessor = data.project.isProfessor;
        b.fabAddTask.setVisibility(isProfessor ? View.VISIBLE : View.GONE);
        b.fabAddTask.setOnClickListener(v -> openAddTask());

        b.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        b.btnAddFirstTask.setVisibility(empty && isProfessor ? View.VISIBLE : View.GONE);
        b.btnAddFirstTask.setOnClickListener(v -> openAddTask());
    }

    private void openAddTask() {
        Intent i = new Intent(this, AddTaskActivity.class);
        i.putExtra(EXTRA_PROJECT_ID, projectId);
        startActivity(i);
    }

    @Override
    public void onStatusClick(TasksPayload.TaskItem task) {
        String[] options = new String[]{
                getString(R.string.task_todo),
                getString(R.string.status_in_progress),
                getString(R.string.task_done)
        };
        String[] values = new String[]{"todo", "in_progress", "done"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.status_update)
                .setItems(options, (dialog, which) -> updateStatus(task.id, values[which]))
                .show();
    }

    private void updateStatus(int taskId, String status) {
        ApiClient.get(this).updateTaskStatus(taskId, status)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                        ApiResponse<Map<String, Object>> body = response.body();
                        if (response.isSuccessful() && body != null) {
                            load();
                        } else {
                            ApiUtils.toast(TasksActivity.this,
                                    (body != null && body.message != null) ? body.message : getString(R.string.generic_error));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        ApiUtils.toast(TasksActivity.this, getString(R.string.network_error));
                    }
                });
    }
}
