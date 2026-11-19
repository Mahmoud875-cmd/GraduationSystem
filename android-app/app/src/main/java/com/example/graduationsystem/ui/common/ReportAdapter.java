package com.example.graduationsystem.ui.common;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.model.ReportsPayload;
import com.example.graduationsystem.databinding.ItemReportBinding;
import com.example.graduationsystem.util.ApiUtils;

import java.util.ArrayList;
import java.util.List;

/** Renders project reports with download and (professor) inline feedback actions. */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {

    public interface Listener {
        void onDownload(ReportsPayload.ReportItem report);

        void onFeedback(ReportsPayload.ReportItem report, String text);
    }

    private final List<ReportsPayload.ReportItem> items = new ArrayList<>();
    private final boolean isProfessor;
    private final String supervisorName;
    private final Listener listener;

    public ReportAdapter(boolean isProfessor, String supervisorName, Listener listener) {
        this.isProfessor = isProfessor;
        this.supervisorName = supervisorName;
        this.listener = listener;
    }

    public void submit(List<ReportsPayload.ReportItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportBinding b = ItemReportBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ReportsPayload.ReportItem r = items.get(position);
        ItemReportBinding b = holder.binding;
        Context ctx = b.getRoot().getContext();

        boolean taskLinked = !TextUtils.isEmpty(r.taskTitle);
        boolean hasNote = !TextUtils.isEmpty(r.feedback);

        // Type chip: task (indigo) vs general (neutral).
        b.typeChipIcon.setImageResource(taskLinked ? R.drawable.ic_task_alt : R.drawable.ic_article);
        b.typeChipText.setText(ctx.getString(
                taskLinked ? R.string.report_type_task : R.string.report_type_general));
        if (taskLinked) {
            b.typeChip.setBackgroundResource(R.drawable.chip_type_task);
            b.typeChipIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.indigo_700));
            b.typeChipText.setTextColor(ContextCompat.getColor(ctx, R.color.indigo_700));
        } else {
            b.typeChip.setBackgroundResource(R.drawable.chip_type_general);
            b.typeChipIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.on_surface_variant));
            b.typeChipText.setTextColor(ContextCompat.getColor(ctx, R.color.on_surface_variant));
        }

        b.reportDate.setText(faDigits(r.submittedAt == null ? "-" : first10(r.submittedAt)));
        b.reportTitle.setText(TextUtils.isEmpty(r.fileName) ? "-" : r.fileName);

        // Attachment box: file-type tile + name + subtitle + download.
        boolean pdf = r.fileName != null && r.fileName.toLowerCase().endsWith(".pdf");
        b.fileTypeIcon.setImageResource(pdf ? R.drawable.ic_picture_as_pdf : R.drawable.ic_article);
        b.fileTypeIcon.setColorFilter(ContextCompat.getColor(ctx, pdf ? R.color.red_600 : R.color.blue_600));
        ((android.graphics.drawable.GradientDrawable) b.fileTypeTile.getBackground().mutate())
                .setColor(ContextCompat.getColor(ctx, pdf ? R.color.red_100 : R.color.blue_100));
        b.reportFileName.setText(TextUtils.isEmpty(r.fileName) ? "-" : r.fileName);
        b.reportFileSubtitle.setText(taskLinked
                ? ctx.getString(R.string.task_chip_fmt, r.taskTitle)
                : ctx.getString(R.string.report_type_general));

        // Status pill: approved (emerald) when a note exists, else pending (amber).
        b.statusText.setText(ctx.getString(hasNote ? R.string.status_approved : R.string.status_pending_review));
        b.statusText.setTextColor(ContextCompat.getColor(ctx, hasNote ? R.color.emerald_700 : R.color.amber_700));
        b.statusDot.getBackground().mutate()
                .setTint(ContextCompat.getColor(ctx, hasNote ? R.color.emerald_600 : R.color.amber_500));

        // Supervisor review panel vs pending banner.
        b.reviewPanel.setVisibility(hasNote ? View.VISIBLE : View.GONE);
        b.pendingBanner.setVisibility(hasNote ? View.GONE : View.VISIBLE);
        if (hasNote) {
            b.reportFeedback.setText(r.feedback);
            b.supervisorName.setText(TextUtils.isEmpty(supervisorName)
                    ? ctx.getString(R.string.supervisor_role_label) : supervisorName);
            b.supervisorAvatar.setText(initial(supervisorName));
        }

        b.commentRow.setVisibility(isProfessor ? View.VISIBLE : View.GONE);
        b.inputComment.setText("");
        b.btnDownload.setOnClickListener(v -> {
            if (listener != null) listener.onDownload(r);
        });
        b.btnFeedback.setOnClickListener(v -> {
            String text = b.inputComment.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                ApiUtils.toast(ctx, ctx.getString(R.string.hint_write_comment));
                return;
            }
            if (listener != null) listener.onFeedback(r, text);
        });
    }

    /** Keep only the date part of an ISO timestamp like 2025-11-20T... */
    private static String first10(String iso) {
        return iso.length() >= 10 ? iso.substring(0, 10) : iso;
    }

    private static String initial(String name) {
        if (TextUtils.isEmpty(name)) return "؟";
        return name.trim().substring(0, 1);
    }

    /** Convert ASCII digits to Persian digits for display. */
    private static String faDigits(String s) {
        char[] d = s.toCharArray();
        StringBuilder sb = new StringBuilder(d.length);
        for (char c : d) sb.append(c >= '0' && c <= '9' ? (char) ('\u06F0' + (c - '0')) : c);
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemReportBinding binding;

        VH(ItemReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
