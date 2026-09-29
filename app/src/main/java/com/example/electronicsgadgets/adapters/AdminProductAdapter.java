package com.example.electronicsgadgets.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.electronicsgadgets.R;
import com.example.electronicsgadgets.databinding.ItemAdminProductBinding;
import com.example.electronicsgadgets.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {

    public interface OnAdminProductActionListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    private final Context context;
    private List<Product> productList;
    private final OnAdminProductActionListener listener;

    public AdminProductAdapter(Context context, List<Product> productList, OnAdminProductActionListener listener) {
        this.context = context;
        this.productList = productList != null ? productList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateProducts(List<Product> newProducts) {
        this.productList = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminProductBinding binding = ItemAdminProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.binding.tvProductName.setText(product.getName());
        holder.binding.tvBrand.setText(product.getBrand());
        holder.binding.tvCategory.setText(product.getCategory());
        holder.binding.tvPrice.setText(String.format(Locale.US, "$%.2f", product.getPrice()));
        holder.binding.tvStock.setText("Stock: " + product.getStock());
        holder.binding.tvRating.setText(String.format(Locale.US, "%.1f", product.getRating()));

        if (product.isFeatured()) {
            holder.binding.tvFeaturedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvFeaturedBadge.setVisibility(View.GONE);
        }

        if (product.getOriginalPrice() > product.getPrice()) {
            holder.binding.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.binding.tvOriginalPrice.setText(String.format(Locale.US, "$%.2f", product.getOriginalPrice()));
            holder.binding.tvOriginalPrice.setPaintFlags(holder.binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.binding.tvOriginalPrice.setVisibility(View.GONE);
        }

        if (product.getDiscount() > 0) {
            holder.binding.tvDiscount.setVisibility(View.VISIBLE);
            holder.binding.tvDiscount.setText(String.format(Locale.US, "-%d%%", product.getDiscount()));
        } else {
            holder.binding.tvDiscount.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_placeholder_product)
                .error(R.drawable.ic_placeholder_product)
                .into(holder.binding.imgProduct);

        holder.binding.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(product);
            }
        });

        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    static class AdminProductViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminProductBinding binding;

        AdminProductViewHolder(ItemAdminProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
