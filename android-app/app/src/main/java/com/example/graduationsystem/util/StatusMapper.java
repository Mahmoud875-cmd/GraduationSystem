package com.example.graduationsystem.util;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.example.graduationsystem.R;

/** Maps backend status keys to Persian labels and Stitch chip drawables/colors. */
public final class StatusMapper {
    private StatusMapper() {}

    @StringRes
    public static int projectStatusLabel(String status) {
        if (status == null) return R.string.status_pending;
        switch (status) {
            case "approved": return R.string.status_approved;
            case "rejected": return R.string.status_rejected;
            case "in_progress": return R.string.status_in_progress;
            case "submitted": return R.string.status_submitted;
            case "evaluated": return R.string.status_evaluated;
            case "pending":
            default: return R.string.status_pending;
        }
    }

    @DrawableRes
    public static int projectStatusChipBg(String status) {
        if (status == null) return R.drawable.chip_pending;
        switch (status) {
            case "approved":
            case "evaluated":
                return R.drawable.chip_approved;
            case "rejected":
                return R.drawable.chip_rejected;
            case "in_progress":
            case "submitted":
                return R.drawable.chip_in_progress;
            case "pending":
            default:
                return R.drawable.chip_pending;
        }
    }

    @ColorRes
    public static int projectStatusTextColor(String status) {
        if (status == null) return R.color.status_pending_fg;
        switch (status) {
            case "approved":
            case "evaluated":
                return R.color.status_approved_fg;
            case "rejected":
                return R.color.status_rejected_fg;
            case "in_progress":
            case "submitted":
                return R.color.status_in_progress_fg;
            case "pending":
            default:
                return R.color.status_pending_fg;
        }
    }

    @StringRes
    public static int taskStatusLabel(String status) {
        if (status == null) return R.string.task_todo;
        switch (status) {
            case "in_progress": return R.string.status_in_progress;
            case "done": return R.string.task_done;
            case "late": return R.string.task_late;
            case "todo":
            default: return R.string.task_todo;
        }
    }

    @DrawableRes
    public static int taskStatusChipBg(String status) {
        if (status == null) return R.drawable.chip_neutral;
        switch (status) {
            case "done": return R.drawable.chip_approved;
            case "in_progress": return R.drawable.chip_in_progress;
            case "late": return R.drawable.chip_rejected;
            case "todo":
            default: return R.drawable.chip_neutral;
        }
    }

    @StringRes
    public static int roleLabel(String role) {
        if ("professor".equals(role)) return R.string.role_professor;
        if ("admin".equals(role)) return R.string.role_admin;
        return R.string.role_student;
    }
}
