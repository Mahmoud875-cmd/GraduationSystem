package com.example.graduationsystem.ui.admin;

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
import com.example.graduationsystem.data.model.AdminUser;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.databinding.ActivityAdminUsersBinding;
import com.example.graduationsystem.util.ApiUtils;
import com.example.graduationsystem.util.FormDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Admin lists all users, filters by role chips and searches by name/email. */
public class AdminUsersActivity extends BaseActivity implements AdminUserAdapter.Listener {

    private static final String MODE_ALL = "all";
    private static final String MODE_STUDENT = "student";
    private static final String MODE_PROFESSOR = "professor";
    private static final String MODE_ADMIN = "admin";

    private ActivityAdminUsersBinding b;
    private AdminUserAdapter adapter;
    private final List<AdminUser> all = new ArrayList<>();
    private String filterMode = MODE_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAdminUsersBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        adapter = new AdminUserAdapter(this);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        b.btnBack.setOnClickListener(v -> finish());
        b.btnAddUser.setOnClickListener(v -> showCreateUserDialog());
        b.swipeRefresh.setOnRefreshListener(this::load);

        setupChip(b.chipAll, b.chipAllLabel, b.chipAllCount, MODE_ALL);
        setupChip(b.chipStudents, b.chipStudentsLabel, b.chipStudentsCount, MODE_STUDENT);
        setupChip(b.chipAdvisors, b.chipAdvisorsLabel, b.chipAdvisorsCount, MODE_PROFESSOR);
        setupChip(b.chipAdmins, b.chipAdminsLabel, b.chipAdminsCount, MODE_ADMIN);

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

    private void setupChip(LinearLayout chipRoot, TextView label, TextView countView, String mode) {
        chipRoot.setOnClickListener(v -> {
            filterMode = mode;
            updateChipStates();
            applyFilter();
        });
    }

    private void updateChipStates() {
        styleChip(b.chipAll, b.chipAllLabel, b.chipAllCount, MODE_ALL.equals(filterMode));
        styleChip(b.chipStudents, b.chipStudentsLabel, b.chipStudentsCount, MODE_STUDENT.equals(filterMode));
        styleChip(b.chipAdvisors, b.chipAdvisorsLabel, b.chipAdvisorsCount, MODE_PROFESSOR.equals(filterMode));
        styleChip(b.chipAdmins, b.chipAdminsLabel, b.chipAdminsCount, MODE_ADMIN.equals(filterMode));
    }

    private void styleChip(LinearLayout root, TextView label, TextView count, boolean selected) {
        int bg = selected ? R.drawable.bg_filter_chip_sel : R.drawable.bg_filter_chip_unsel;
        int fg = selected ? R.color.on_primary_container : R.color.on_surface_variant;
        int cntBg = selected ? R.drawable.bg_chip_count_sel : R.drawable.bg_chip_count_unsel;
        root.setBackgroundResource(bg);
        label.setTextColor(ContextCompat.getColor(this, fg));
        count.setBackgroundResource(cntBg);
        count.setTextColor(ContextCompat.getColor(this, fg));
    }

    private void load() {
        b.loading.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
        ApiClient.get(this).adminUsers().enqueue(new Callback<ApiResponse<List<AdminUser>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AdminUser>>> call, Response<ApiResponse<List<AdminUser>>> response) {
                try {
                    b.swipeRefresh.setRefreshing(false);
                    b.loading.setVisibility(View.GONE);
                    ApiResponse<List<AdminUser>> body = response.body();
                    if (response.isSuccessful() && body != null && body.data != null) {
                        all.clear();
                        all.addAll(body.data);
                        updateCounts();
                        applyFilter();
                    } else {
                        String msg;
                        if (body != null && body.message != null) {
                            msg = body.message;
                        } else if (response.errorBody() != null) {
                            msg = getString(R.string.network_error);
                        } else {
                            msg = getString(R.string.network_error);
                        }
                        ApiUtils.toast(AdminUsersActivity.this, msg);
                    }
                } catch (Exception e) {
                    ApiUtils.toast(AdminUsersActivity.this, getString(R.string.network_error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AdminUser>>> call, Throwable t) {
                b.swipeRefresh.setRefreshing(false);
                b.loading.setVisibility(View.GONE);
                ApiUtils.toast(AdminUsersActivity.this, getString(R.string.network_error));
            }
        });
    }

    private void updateCounts() {
        int students = 0, professors = 0, admins = 0;
        for (AdminUser u : all) {
            if ("student".equals(u.role)) students++;
            else if ("professor".equals(u.role)) professors++;
            else if ("admin".equals(u.role)) admins++;
        }
        b.chipAllCount.setText(faDigits(String.valueOf(all.size())));
        b.chipStudentsCount.setText(faDigits(String.valueOf(students)));
        b.chipAdvisorsCount.setText(faDigits(String.valueOf(professors)));
        b.chipAdminsCount.setText(faDigits(String.valueOf(admins)));
    }

    /** Combines role-mode + text query into the visible list. */
    private void applyFilter() {
        String q = b.inputSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<AdminUser> filtered = new ArrayList<>();
        for (AdminUser u : all) {
            // Role mode filter
            if (!MODE_ALL.equals(filterMode) && !filterMode.equals(u.role)) continue;
            // Text search filter
            if (!q.isEmpty()) {
                boolean match = (u.name != null && u.name.toLowerCase(Locale.ROOT).contains(q))
                        || (u.email != null && u.email.toLowerCase(Locale.ROOT).contains(q));
                if (!match) continue;
            }
            filtered.add(u);
        }
        adapter.submit(filtered);
    }

    @Override
    public void onEdit(AdminUser user) {
        FormDialog form = FormDialog.build(this)
                .text("name", getString(R.string.label_fullname_short), user.name, false, false)
                .text("email", getString(R.string.label_email), user.email, false, false)
                .dropdown("role", getString(R.string.label_role), roleLabel(user.role), roleLabels())
                .text("password", getString(R.string.password_optional_hint), null, false, true);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.edit_user_fmt, user.name))
                .setView(form.getRoot())
                .setPositiveButton(R.string.save, (d, w) -> {
                    String name = form.value("name");
                    String email = form.value("email");
                    if (name == null || email == null) {
                        ApiUtils.toast(this, getString(R.string.field_required_title));
                        return;
                    }
                    String role = resolveRoleKey(form.value("role"));
                    String password = form.value("password");
                    ApiClient.get(this).updateAdminUser(user.id, name, email, role, password)
                            .enqueue(new Callback<ApiResponse<AdminUser>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<AdminUser>> call, Response<ApiResponse<AdminUser>> r) {
                                    if (r.isSuccessful() && r.body() != null && r.body().data != null) {
                                        ApiUtils.toast(AdminUsersActivity.this, getString(R.string.action_saved_toast));
                                        load();
                                    } else {
                                        ApiUtils.toast(AdminUsersActivity.this, message(r.body()));
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<AdminUser>> call, Throwable t) {
                                    ApiUtils.toast(AdminUsersActivity.this, getString(R.string.network_error));
                                }
                            });
                })
                .setNeutralButton(R.string.delete, (d, w) -> confirmDelete(user))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDelete(AdminUser user) {
        confirmDelete(user);
    }

    private void showCreateUserDialog() {
        FormDialog form = FormDialog.build(this)
                .text("name", getString(R.string.label_fullname_short), null, false, false)
                .text("email", getString(R.string.label_email), null, false, false)
                .dropdown("role", getString(R.string.label_role), roleLabel("student"), roleLabels())
                .text("password", getString(R.string.label_password), null, false, true);

        new AlertDialog.Builder(this)
                .setTitle(R.string.new_user_title)
                .setView(form.getRoot())
                .setPositiveButton(R.string.save, (d, w) -> {
                    String name = form.value("name");
                    String email = form.value("email");
                    String password = form.value("password");
                    if (name == null || email == null || password == null) {
                        ApiUtils.toast(this, getString(R.string.field_required_title));
                        return;
                    }
                    String role = resolveRoleKey(form.value("role"));
                    ApiClient.get(this).createAdminUser(name, email, password, role)
                            .enqueue(new Callback<ApiResponse<AdminUser>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<AdminUser>> call, Response<ApiResponse<AdminUser>> r) {
                                    if (r.isSuccessful() && r.body() != null && r.body().data != null) {
                                        ApiUtils.toast(AdminUsersActivity.this, getString(R.string.action_saved_toast));
                                        load();
                                    } else {
                                        ApiUtils.toast(AdminUsersActivity.this, message(r.body()));
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<AdminUser>> call, Throwable t) {
                                    ApiUtils.toast(AdminUsersActivity.this, getString(R.string.network_error));
                                }
                            });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(AdminUser user) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_user_fmt, user.name))
                .setPositiveButton(R.string.delete, (d, w) ->
                        ApiClient.get(this).deleteAdminUser(user.id).enqueue(new Callback<ApiResponse<Object>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> r) {
                                if (r.isSuccessful()) {
                                    ApiUtils.toast(AdminUsersActivity.this, getString(R.string.action_deleted_toast));
                                    load();
                                } else {
                                    ApiUtils.toast(AdminUsersActivity.this, getString(R.string.action_failed_toast));
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                ApiUtils.toast(AdminUsersActivity.this, getString(R.string.network_error));
                            }
                        }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ---------------- role helpers ----------------

    private static final List<String> ROLE_KEYS = Arrays.asList("student", "professor", "admin");

    private List<String> roleLabels() {
        List<String> labels = new ArrayList<>();
        for (String k : ROLE_KEYS) labels.add(roleLabel(k));
        return labels;
    }

    private String roleLabel(String key) {
        if ("professor".equals(key)) return getString(R.string.role_professor);
        if ("admin".equals(key)) return getString(R.string.role_admin);
        return getString(R.string.role_student);
    }

    /** Maps a Persian role label back to its backend key; defaults to student. */
    private String resolveRoleKey(String label) {
        int idx = roleLabels().indexOf(label);
        return idx < 0 ? "student" : ROLE_KEYS.get(idx);
    }

    private String message(ApiResponse<?> body) {
        return (body != null && body.message != null) ? body.message : getString(R.string.action_failed_toast);
    }

    private static String faDigits(String in) {
        StringBuilder sb = new StringBuilder();
        for (char c : in.toCharArray()) {
            sb.append(c >= '0' && c <= '9' ? (char) ('\u06F0' + (c - '0')) : c);
        }
        return sb.toString();
    }
}
