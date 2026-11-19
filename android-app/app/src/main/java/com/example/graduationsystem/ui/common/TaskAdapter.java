package com.example.graduationsystem.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.model.TasksPayload;
import com.example.graduationsystem.databinding.ItemTaskBinding;
import com.example.graduationsystem.util.StatusMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Stitch layout: position 0 is the header + overall progress card, then one card per task.
 * Optionally exposes a status-change action (professor view).
 */
public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnStatusClick {
        void onStatusClick(TasksPayload.TaskItem task);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;

    private final List<TasksPayload.TaskItem> items = new ArrayList<>();
    private final boolean showStatusButton;
    private final OnStatusClick onStatusClick;
    private int progressPercent;

    public TaskAdapter(boolean showStatusButton, OnStatusClick onStatusClick) {
        this.showStatusButton = showStatusButton;
        this.onStatusClick = onStatusClick;
    }

    public void submit(List<TasksPayload.TaskItem> newItems, int percent) {
        this.progressPercent = percent;
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_TASK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding b = ItemTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return viewType == TYPE_HEADER ? new HeaderVH(b) : new TaskVH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemTaskBinding b = ((BaseVH) holder).binding;
        if (holder instanceof HeaderVH) {
            bindHeader(b);
            return;
        }
        // Guard: never index a task that does not exist (prevents blank/crashed cards).
        int taskIndex = position - 1;
        if (taskIndex < 0 || taskIndex >= items.size()) {
            b.headerSection.setVisibility(View.GONE);
            b.progressCard.setVisibility(View.GONE);
            return;
        }
        TasksPayload.TaskItem t = items.get(taskIndex);
        bindTask(b, t);
    }

    private void bindHeader(ItemTaskBinding b) {
        b.headerSection.setVisibility(View.VISIBLE);
        b.progressCard.setVisibility(View.VISIBLE);

        int total = items.size();
        int done = 0;
        for (TasksPayload.TaskItem t : items) {
            if ("done".equals(t.status)) done++;
        }
        android.content.Context ctx = b.getRoot().getContext();
        b.progressPercent.setText(ctx.getString(R.string.percent_fmt, toFa(progressPercent)));
        b.overallProgressBar.setProgress(progressPercent);
        b.progressCounter.setText(ctx.getString(R.string.tasks_completed_of_fmt,
                toFa(done), toFa(total)));
    }

    private void bindTask(ItemTaskBinding b, TasksPayload.TaskItem t) {
        b.headerSection.setVisibility(View.GONE);
        b.progressCard.setVisibility(View.GONE);

        android.content.Context ctx = b.getRoot().getContext();
        b.taskTitle.setText(t.title == null || t.title.isEmpty() ? "-" : t.title);

        // Hide description entirely when empty to avoid blank space
        if (t.description == null || t.description.trim().isEmpty()) {
            b.taskDescription.setVisibility(View.GONE);
        } else {
            b.taskDescription.setVisibility(View.VISIBLE);
            b.taskDescription.setText(t.description);
        }

        b.taskDeadline.setText(ctx.getString(R.string.task_deadline_fmt,
                t.deadline == null ? "-" : toFa(t.deadline)));
        b.taskAssignee.setText(ctx.getString(R.string.assigned_to_fmt,
                t.assigneeName == null ? "-" : t.assigneeName));
        b.assigneeAvatar.setText(initials(t.assigneeName));

        // Phase pill: hide if no meaningful phase info
        String phase = phaseOf(t.title);
        if (phase == null || phase.isEmpty()) {
            b.taskPhase.setVisibility(View.GONE);
        } else {
            b.taskPhase.setVisibility(View.VISIBLE);
            b.taskPhase.setText(phase);
            b.taskPhase.setBackgroundResource(phaseBg(t.status));
            b.taskPhase.setTextColor(ContextCompat.getColor(ctx, phaseFg(t.status)));
        }

        // Status chip: gray pill + colored dot (Stitch).
        b.taskStatusChip.setText(StatusMapper.taskStatusLabel(t.status));
        b.taskStatusChip.setTextColor(ContextCompat.getColor(ctx, statusChipFg(t.status)));
        if (b.statusDot.getBackground() != null) {
            b.statusDot.getBackground().mutate()
                    .setTint(ContextCompat.getColor(ctx, statusDotColor(t.status)));
        }

        b.btnChangeStatus.setText(StatusMapper.taskStatusLabel(t.status));
        b.statusSelectRow.setVisibility(showStatusButton ? View.VISIBLE : View.GONE);
        b.btnChangeStatus.setOnClickListener(v -> {
            if (onStatusClick != null) onStatusClick.onStatusClick(t);
        });
    }

    /** Phase pill label; returns empty when title has no phase keyword. */
    private static String phaseOf(String title) {
        if (title == null || title.trim().isEmpty()) return "";
        // Only show phase badge if the task name starts with a recognizable pattern
        if (title.startsWith("فاز") || title.startsWith("Phase")) return title;
        return "وظیفه";
    }

    @DrawableRes
    private static int phaseBg(String status) {
        if (status == null) return R.drawable.chip_phase_surface;
        switch (status) {
            case "in_progress": return R.drawable.chip_phase_primary;
            case "done": return R.drawable.chip_phase_tertiary;
            case "late": return R.drawable.chip_rejected;
            case "todo":
            default: return R.drawable.chip_phase_surface;
        }
    }

    private static int phaseFg(String status) {
        if (status == null) return R.color.on_surface_variant;
        switch (status) {
            case "in_progress": return R.color.primary;
            case "done": return R.color.tertiary;
            case "late": return R.color.error;
            case "todo":
            default: return R.color.on_surface_variant;
        }
    }

    private static int statusChipFg(String status) {
        if ("in_progress".equals(status)) return R.color.on_surface;
        if ("done".equals(status)) return R.color.on_surface;
        return R.color.on_surface_variant;
    }

    private static int statusDotColor(String status) {
        if (status == null) return R.color.outline;
        switch (status) {
            case "in_progress": return R.color.primary;
            case "done": return R.color.emerald_600;
            case "late": return R.color.error;
            case "todo":
            default: return R.color.outline;
        }
    }

    /** First letters of the assignee's name, e.g. "علی محمد" → "ع‌م". */
    private static String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "-";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        int take = Math.min(parts.length, 2);
        for (int i = 0; i < take; i++) {
            if (i > 0) sb.append('\u200C'); // ZWNJ separator used in Persian initials
            sb.append(parts[i].charAt(0));
        }
        return sb.toString();
    }

    /** Converts ASCII digits inside a string to Persian digits (Stitch typography). */
    private static String toFa(String s) {
        if (s == null) return "";
        char[] c = s.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] >= '0' && c[i] <= '9') c[i] = (char) ('\u06F0' + (c[i] - '0'));
        }
        return new String(c);
    }

    private static String toFa(int n) {
        return toFa(String.valueOf(n));
    }

    @Override
    public int getItemCount() {
        return items.isEmpty() ? 0 : items.size() + 1;
    }

    abstract static class BaseVH extends RecyclerView.ViewHolder {
        final ItemTaskBinding binding;

        BaseVH(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class HeaderVH extends BaseVH {
        HeaderVH(ItemTaskBinding binding) {
            super(binding);
        }
    }

    static class TaskVH extends BaseVH {
        TaskVH(ItemTaskBinding binding) {
            super(binding);
        }
    }
}
