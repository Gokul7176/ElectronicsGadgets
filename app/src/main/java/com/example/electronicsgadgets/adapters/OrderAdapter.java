package com.example.electronicsgadgets.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.electronicsgadgets.R;
import com.example.electronicsgadgets.databinding.ItemOrderBinding;
import com.example.electronicsgadgets.models.Order;
import com.example.electronicsgadgets.models.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private final Context context;
    private List<Order> orderList;
    private final OnOrderClickListener listener;

    public OrderAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList != null ? orderList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateOrders(List<Order> newOrders) {
        this.orderList = newOrders != null ? newOrders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.binding.tvOrderId.setText(String.format("Order #%s", order.getOrderId()));
        holder.binding.tvOrderDate.setText(String.format("Placed on %s", order.getOrderDate()));
        holder.binding.tvOrderTotal.setText(String.format(Locale.US, "$%.2f", order.getTotalAmount()));

        // Format items summary
        StringBuilder summary = new StringBuilder();
        int totalItemsCount = 0;
        if (order.getItems() != null) {
            for (int i = 0; i < order.getItems().size(); i++) {
                OrderItem item = order.getItems().get(i);
                totalItemsCount += item.getQuantity();
                if (i > 0) summary.append(", ");
                summary.append(item.getProductName());
            }
        }
        holder.binding.tvOrderItemsSummary.setText(String.format(Locale.US, "%d Items: %s", totalItemsCount, summary.toString()));

        // Status badge & color
        String status = order.getStatus() != null ? order.getStatus() : "Pending";
        holder.binding.tvOrderStatus.setText(status);

        int statusColor;
        switch (status.toLowerCase()) {
            case "confirmed":
                statusColor = R.color.status_confirmed;
                break;
            case "shipped":
                statusColor = R.color.status_shipped;
                break;
            case "delivered":
                statusColor = R.color.status_delivered;
                break;
            case "cancelled":
                statusColor = R.color.status_cancelled;
                break;
            case "pending":
            default:
                statusColor = R.color.status_pending;
                break;
        }
        holder.binding.tvOrderStatus.setTextColor(ContextCompat.getColor(context, statusColor));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        final ItemOrderBinding binding;

        OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
