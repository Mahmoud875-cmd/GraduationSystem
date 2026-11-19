package com.example.graduationsystem.ui.professor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.model.DashboardData;
import com.example.graduationsystem.data.model.ProfessorProject;
import com.example.graduationsystem.databinding.ItemTopicBinding;

import java.util.ArrayList;
import java.util.List;

/** Renders project topics: pending ones get inline dates + approve/reject, approved ones a info row. */
public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.VH> {

    public interface Listener {
        void onApprove(ProfessorProject project);

        void onReject(ProfessorProject project);

        void onPickStartDate(ProfessorProject project);

        void onPickEndDate(ProfessorProject project);
    }

    /** Pending date picks per project, shown in the inline fields before approval. */
    public final class DateSelections {
        public String start;
        public String end;
    }

    private final List<ProfessorProject> items = new ArrayList<>();
    private final Listener listener;

    public TopicAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<ProfessorProject> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTopicBinding b = ItemTopicBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ProfessorProject p = items.get(position);
        ItemTopicBinding b = holder.binding;
        android.content.Context ctx = b.getRoot().getContext();

        b.topicTitle.setText(p.title);
        b.topicDescription.setText(p.description == null ? "" : p.description);

        boolean pending = "pending".equals(p.status);
        boolean rejected = "rejected".equals(p.status);

        if (pending) {
            b.statusChip.setText(R.string.status_pending_approval);
            b.statusChip.setBackgroundResource(R.drawable.chip_amber);
            b.statusChip.setTextColor(ContextCompat.getColor(ctx, R.color.amber_800));
        } else if (rejected) {
            b.statusChip.setText(R.string.status_rejected);
            b.statusChip.setBackgroundResource(R.drawable.chip_rejected);
            b.statusChip.setTextColor(ContextCompat.getColor(ctx, R.color.error));
        } else {
            b.statusChip.setText(R.string.status_approved_label);
            b.statusChip.setBackgroundResource(R.drawable.chip_emerald);
            b.statusChip.setTextColor(ContextCompat.getColor(ctx, R.color.emerald_800));
        }

        b.topicTeam.setText(ctx.getString(R.string.team_name_fmt,
                p.groupName == null ? "-" : p.groupName));
        b.topicMembers.setText(ctx.getString(R.string.members_names_fmt, membersNames(p)));

        // Inline date section + actions only for pending topics (matches Stitch).
        b.dateSection.setVisibility(pending ? View.VISIBLE : View.GONE);
        b.actionsRow.setVisibility(pending ? View.VISIBLE : View.GONE);
        b.approvedRow.setVisibility(!pending && !rejected ? View.VISIBLE : View.GONE);

        if (pending) {
            b.startDateField.setOnClickListener(v -> {
                if (listener != null) listener.onPickStartDate(p);
            });
            b.endDateField.setOnClickListener(v -> {
                if (listener != null) listener.onPickEndDate(p);
            });
            DateSelections sel = selectionsFor(p.id);
            b.startDateField.setText(sel.start != null ? sel.start : ctx.getString(R.string.choose_option));
            b.endDateField.setText(sel.end != null ? sel.end : ctx.getString(R.string.choose_option));

            b.btnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onApprove(p);
            });
            b.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(p);
            });
        } else if (!rejected) {
            b.approvedRange.setText(ctx.getString(R.string.approved_range_fmt,
                    dashDate(p.startDate), dashDate(p.endDate)));
        }
    }

    private final java.util.Map<Integer, DateSelections> selections = new java.util.HashMap<>();

    public DateSelections selectionsFor(int projectId) {
        DateSelections s = selections.get(projectId);
        if (s == null) {
            s = new DateSelections();
            selections.put(projectId, s);
        }
        return s;
    }

    /** Removes picked dates after approve/reject so fields reset. */
    public void clearSelections(int projectId) {
        selections.remove(projectId);
    }

    private static String membersNames(ProfessorProject p) {
        if (p.teamMembers == null || p.teamMembers.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (DashboardData.Member m : p.teamMembers) {
            if (sb.length() > 0) sb.append("، ");
            sb.append(m.name);
        }
        return sb.toString();
    }

    private static String dashDate(String iso) {
        return (iso == null || iso.isEmpty()) ? "-" : iso.replace('-', '/');
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemTopicBinding binding;

        VH(ItemTopicBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
