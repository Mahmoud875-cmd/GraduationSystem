package com.example.graduationsystem.ui.professor;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.model.DashboardData;
import com.example.graduationsystem.databinding.ItemProjectActiveBinding;
import com.example.graduationsystem.util.StatusMapper;

import java.util.ArrayList;
import java.util.List;

/** Lists a professor's active projects with task/report/evaluate actions. */
public class ActiveProjectAdapter extends RecyclerView.Adapter<ActiveProjectAdapter.VH> {

    public interface Listener {
        void onTasks(DashboardData.Project project);

        void onReports(DashboardData.Project project);

        void onEvaluate(DashboardData.Project project);
    }

    private final List<DashboardData.Project> items = new ArrayList<>();
    private final Listener listener;

    public ActiveProjectAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<DashboardData.Project> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProjectActiveBinding b = ItemProjectActiveBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DashboardData.Project p = items.get(position);
        ItemProjectActiveBinding b = holder.binding;
        b.projectTitle.setText(p.title);
        b.groupName.setText(b.getRoot().getContext().getString(R.string.team_of_fmt,
                p.groupName == null ? "-" : p.groupName));

        b.statusChip.setText(StatusMapper.projectStatusLabel(p.status));
        b.statusChip.setBackgroundResource(StatusMapper.projectStatusChipBg(p.status));
        b.statusChip.setTextColor(ContextCompat.getColor(b.getRoot().getContext(),
                StatusMapper.projectStatusTextColor(p.status)));

        b.progressValue.setText(toPersianDigits(p.progressPercent) + "٪");
        b.progressBar.setProgress(p.progressPercent);

        b.btnTasks.setOnClickListener(v -> {
            if (listener != null) listener.onTasks(p);
        });
        b.btnReports.setOnClickListener(v -> {
            if (listener != null) listener.onReports(p);
        });
        b.btnEvaluate.setOnClickListener(v -> {
            if (listener != null) listener.onEvaluate(p);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String toPersianDigits(int in) {
        String s = Integer.toString(in);
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append(c >= '0' && c <= '9' ? (char) ('۰' + (c - '0')) : c);
        }
        return sb.toString();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemProjectActiveBinding binding;

        VH(ItemProjectActiveBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
