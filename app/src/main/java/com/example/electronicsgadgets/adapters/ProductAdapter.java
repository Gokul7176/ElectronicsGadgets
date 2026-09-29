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
import com.example.electronicsgadgets.databinding.ItemProductBinding;
import com.example.electronicsgadgets.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddToCartClick(Product product);
    }

    private final Context context;
    private List<Product> productList;
    private final OnProductClickListener listener;

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
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
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.binding.tvProductName.setText(product.getName());
        holder.binding.tvProductBrand.setText(product.getBrand());
        holder.binding.tvRating.setText(String.format(Locale.US, "%.1f", product.getRating()));
        holder.binding.tvPrice.setText(String.format(Locale.US, "$%.2f", product.getPrice()));

        if (product.getOriginalPrice() > product.getPrice()) {
            holder.binding.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.binding.tvOriginalPrice.setText(String.format(Locale.US, "$%.2f", product.getOriginalPrice()));
            holder.binding.tvOriginalPrice.setPaintFlags(holder.binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.binding.tvOriginalPrice.setVisibility(View.GONE);
        }

        if (product.getDiscount() > 0) {
            holder.binding.tvDiscountBadge.setVisibility(View.VISIBLE);
            holder.binding.tvDiscountBadge.setText(String.format(Locale.US, "-%d%%", product.getDiscount()));
        } else {
            holder.binding.tvDiscountBadge.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_placeholder_product)
                .error(R.drawable.ic_placeholder_product)
                .into(holder.binding.imgProduct);

        holder.binding.cardProduct.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        holder.binding.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCartClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ItemProductBinding binding;

        ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
