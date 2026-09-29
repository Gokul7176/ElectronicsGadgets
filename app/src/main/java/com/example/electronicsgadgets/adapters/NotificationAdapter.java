package com.example.electronicsgadgets.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.electronicsgadgets.R;
import com.example.electronicsgadgets.databinding.ItemNotificationBinding;
import com.example.electronicsgadgets.models.NotificationModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private List<NotificationModel> notifications;
    private final OnNotificationClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.US);

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification);
    }

    public NotificationAdapter(Context context, List<NotificationModel> notifications, OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    public void updateNotifications(List<NotificationModel> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(context), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        public NotificationViewHolder(@NonNull ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(NotificationModel notification) {
            binding.tvNotificationTitle.setText(notification.getTitle());
            binding.tvNotificationMessage.setText(notification.getMessage());

            if (notification.getTimestamp() > 0) {
                CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                        notification.getTimestamp(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                );
                binding.tvNotificationDate.setText(timeAgo);
            } else {
                binding.tvNotificationDate.setText("");
            }

            if (!notification.isRead()) {
                binding.cardNotification.setCardBackgroundColor(Color.parseColor("#F0F9FF")); // Light blue accent
                binding.tvNotificationTitle.setTypeface(null, Typeface.BOLD);
                binding.viewUnreadDot.setVisibility(View.VISIBLE);
            } else {
                binding.cardNotification.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                binding.tvNotificationTitle.setTypeface(null, Typeface.NORMAL);
                binding.viewUnreadDot.setVisibility(View.GONE);
            }

            // Set type icon
            String type = notification.getType() != null ? notification.getType().toLowerCase() : "";
            if ("cart".equals(type)) {
                binding.ivNotificationIcon.setImageResource(R.drawable.ic_cart);
            } else if ("order".equals(type)) {
                binding.ivNotificationIcon.setImageResource(R.drawable.ic_orders);
            } else if ("product".equals(type)) {
                binding.ivNotificationIcon.setImageResource(R.drawable.ic_products);
            } else {
                binding.ivNotificationIcon.setImageResource(R.drawable.ic_notifications);
            }

            binding.cardNotification.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }
    }
}
