package com.example.electronicsgadgets.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.electronicsgadgets.R;
import com.example.electronicsgadgets.databinding.ItemCartBinding;
import com.example.electronicsgadgets.models.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemChangeListener {
        void onQuantityChanged(CartItem cartItem, int newQuantity);
        void onItemRemoved(CartItem cartItem);
    }

    private final Context context;
    private List<CartItem> cartItems;
    private final OnCartItemChangeListener listener;

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemChangeListener listener) {
        this.context = context;
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        this.listener = listener;
    }

    public void updateCartItems(List<CartItem> newItems) {
        this.cartItems = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.binding.tvCartProductName.setText(item.getProductName());
        holder.binding.tvCartProductBrand.setText(item.getProductBrand());
        holder.binding.tvCartProductPrice.setText(String.format(Locale.US, "$%.2f", item.getPrice() * item.getQuantity()));
        holder.binding.tvCartQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_placeholder_product)
                .error(R.drawable.ic_placeholder_product)
                .into(holder.binding.imgCartProduct);

        holder.binding.btnIncreaseQty.setOnClickListener(v -> {
            if (listener != null) {
                if (item.getQuantity() < item.getStockQuantity()) {
                    listener.onQuantityChanged(item, item.getQuantity() + 1);
                }
            }
        });

        holder.binding.btnDecreaseQty.setOnClickListener(v -> {
            if (listener != null) {
                if (item.getQuantity() > 1) {
                    listener.onQuantityChanged(item, item.getQuantity() - 1);
                } else {
                    listener.onItemRemoved(item);
                }
            }
        });

        holder.binding.btnRemoveCartItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemRemoved(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        final ItemCartBinding binding;

        CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
