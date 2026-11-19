package com.example.graduationsystem.ui.admin;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graduationsystem.R;
import com.example.graduationsystem.data.model.AdminUser;
import com.example.graduationsystem.databinding.ItemAdminUserBinding;
import com.example.graduationsystem.util.StatusMapper;

import java.util.ArrayList;
import java.util.List;

/** Renders admin user rows: avatar initials, role chip (color by role), real numeric ID. */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.VH> {

    public interface Listener {
        void onEdit(AdminUser user);

        void onDelete(AdminUser user);
    }

    private final List<AdminUser> items = new ArrayList<>();
    private final Listener listener;

    public AdminUserAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<AdminUser> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminUserBinding b = ItemAdminUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        try {
            AdminUser u = items.get(position);
            Context ctx = holder.binding.getRoot().getContext();
            ItemAdminUserBinding b = holder.binding;

            b.avatarInitials.setText(initials(u.name));
            boolean admin = "admin".equals(u.role);
            b.avatarInitials.setBackgroundResource(admin
                    ? R.drawable.bg_circle_avatar_secondary : R.drawable.bg_circle_avatar_primary);
            b.avatarInitials.setTextColor(ContextCompat.getColor(ctx, admin
                    ? R.color.on_secondary_container : R.color.on_primary_fixed));

            b.userName.setText(u.name == null ? "-" : u.name);
            b.userEmail.setText(u.email == null ? "" : u.email);

            b.roleChip.setText(StatusMapper.roleLabel(u.role));
            b.roleChip.setBackgroundResource("professor".equals(u.role)
                    ? R.drawable.bg_role_chip_secondary : R.drawable.bg_role_chip);

            b.userIdText.setText(ctx.getString(R.string.user_id_fmt, faDigits(String.valueOf(u.id))));

            b.btnEditUser.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(u);
            });
            b.btnDeleteUser.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(u);
            });
            b.cardRoot.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(u);
            });
        } catch (Exception e) {
            // Prevent RecyclerView crash from killing the entire activity
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** First two Persian letters of the name for the avatar circle. */
    private static String initials(String name) {
        if (TextUtils.isEmpty(name)) return "؛";
        String trimmed = name.trim();
        StringBuilder sb = new StringBuilder();
        for (String part : trimmed.split("\\s+")) {
            if (!part.isEmpty()) {
                sb.append(part.charAt(0));
                if (sb.length() >= 2) break;
            }
        }
        return sb.length() == 0 ? trimmed.substring(0, Math.min(1, trimmed.length())) : sb.toString();
    }

    private static String faDigits(String in) {
        StringBuilder sb = new StringBuilder();
        for (char c : in.toCharArray()) {
            sb.append(c >= '0' && c <= '9' ? (char) ('\u06F0' + (c - '0')) : c);
        }
        return sb.toString();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminUserBinding binding;

        VH(ItemAdminUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
