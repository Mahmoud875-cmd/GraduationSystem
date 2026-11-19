package com.example.graduationsystem.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.model.AdminProject;
import com.example.graduationsystem.databinding.ItemAdminProjectBinding;
import com.example.graduationsystem.util.StatusMapper;

import java.util.ArrayList;
import java.util.List;

/** Renders all projects for the admin overview (Stitch "همه پروژه‌ها" cards). */
public class AdminProjectAdapter extends RecyclerView.Adapter<AdminProjectAdapter.VH> {

    /** Card action callbacks. */
    public interface Listener {
        void onManage(AdminProject project);

        void onEvaluate(AdminProject project);

        void onExtendDeadline(AdminProject project);
    }

    private final List<AdminProject> items = new ArrayList<>();
    private final Listener listener;

    public AdminProjectAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<AdminProject> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminProjectBinding b = ItemAdminProjectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AdminProject p = items.get(position);
        Context ctx = holder.binding.getRoot().getContext();
        ItemAdminProjectBinding b = holder.binding;

        b.projectTitle.setText(p.title);
        b.teamLine.setText(ctx.getString(R.string.proj_team_fmt,
                p.groupName == null ? "-" : p.groupName));

        b.professorName.setText(ctx.getString(R.string.proj_professor_fmt,
                p.professorName == null ? "-" : p.professorName));

        // Status chip
        b.statusChip.setText(StatusMapper.projectStatusLabel(p.status));
        b.statusChip.setBackgroundResource(statusChipBg(p.status));
        b.statusChip.setTextColor(ContextCompat.getColor(ctx, statusTextColor(p.status)));

        // Progress
        String pct = faDigits(Integer.toString(p.progressPercent)) + "٪";
        b.progressPct.setText(pct);
        b.progressBar.setProgress(p.progressPercent);

        // Footer: score / awaiting text + action button
        boolean needsEval = "submitted".equals(p.status);
        boolean evaluated = "evaluated".equals(p.status);
        b.footerScore.setVisibility(View.GONE);
        if (evaluated && p.score != null) {
            b.footerText.setText(ctx.getString(R.string.proj_final_score_label));
            b.footerScore.setVisibility(View.VISIBLE);
            b.footerScore.setText(formatScore(p.score));
        } else if (needsEval) {
            b.footerText.setText(ctx.getString(R.string.proj_awaiting_eval));
        } else {
            b.footerText.setText(ctx.getString(R.string.proj_no_score));
        }

        if (needsEval) {
            b.btnAction.setBackgroundResource(R.drawable.bg_action_primary);
            b.actionIcon.setImageResource(R.drawable.ic_grade);
            b.actionIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.on_primary));
            b.actionLabel.setText(ctx.getString(R.string.proj_register_eval));
            b.actionLabel.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary));
        } else {
            b.btnAction.setBackgroundResource(R.drawable.bg_action_secondary);
            b.actionIcon.setImageResource(R.drawable.ic_visibility);
            b.actionIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.on_surface));
            b.actionLabel.setText(ctx.getString(R.string.proj_details_manage));
            b.actionLabel.setTextColor(ContextCompat.getColor(ctx, R.color.on_surface));
        }

        b.btnAction.setOnClickListener(v -> {
            if (listener == null) return;
            if (needsEval) listener.onEvaluate(p); else listener.onManage(p);
        });
        b.btnEditProject.setOnClickListener(v -> {
            if (listener != null) listener.onManage(p);
        });
        b.btnExtend.setOnClickListener(v -> {
            if (listener != null) listener.onExtendDeadline(p);
        });
        b.cardRoot.setOnClickListener(v -> {
            if (listener != null) listener.onManage(p);
        });
    }

    /** Maps a project status to the Stitch card pill background. */
    @androidx.annotation.DrawableRes
    static int statusChipBg(String status) {
        if (status == null) return R.drawable.chip_status_error;
        switch (status) {
            case "in_progress":
            case "approved":
                return R.drawable.chip_status_secondary_fixed;
            case "submitted":
                return R.drawable.chip_amber;
            case "evaluated":
                return R.drawable.chip_emerald;
            case "pending":
            case "rejected":
            default:
                return R.drawable.chip_status_error;
        }
    }

    /** Maps a project status to the Stitch card pill text color. */
    @androidx.annotation.ColorRes
    static int statusTextColor(String status) {
        if (status == null) return R.color.on_error_container;
        switch (status) {
            case "in_progress":
            case "approved":
                return R.color.on_secondary_fixed;
            case "submitted":
                return R.color.amber_800;
            case "evaluated":
                return R.color.emerald_800;
            case "pending":
            case "rejected":
            default:
                return R.color.on_error_container;
        }
    }

    private String formatScore(double score) {
        long rounded = Math.round(score);
        return faDigits(Long.toString(rounded)) + " / ۲۰";
    }

    private String faDigits(String in) {
        StringBuilder sb = new StringBuilder();
        for (char c : in.toCharArray()) {
            sb.append(c >= '0' && c <= '9' ? (char) ('\u06F0' + (c - '0')) : c);
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminProjectBinding binding;

        VH(ItemAdminProjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
