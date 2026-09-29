package com.example.electronicsgadgets.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.electronicsgadgets.R;
import com.example.electronicsgadgets.databinding.ItemCategoryBinding;
import com.example.electronicsgadgets.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    private final Context context;
    private final List<Category> categoryList;
    private final OnCategoryClickListener listener;
    private int selectedPosition = 0;

    public CategoryAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int previous = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previous);
        notifyItemChanged(selectedPosition);
    }

    public void setSelectedCategoryById(String categoryId) {
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getCategoryId().equalsIgnoreCase(categoryId)) {
                setSelectedPosition(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        boolean isSelected = position == selectedPosition;

        holder.binding.tvCategoryName.setText(category.getName());
        holder.binding.imgCategoryIcon.setImageResource(category.getIconResId());

        if (isSelected) {
            holder.binding.cardCategory.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary));
            holder.binding.tvCategoryName.setTextColor(Color.WHITE);
            holder.binding.imgCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.secondary));
        } else {
            holder.binding.cardCategory.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface));
            holder.binding.tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.binding.imgCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            setSelectedPosition(holder.getAdapterPosition());
            if (listener != null) {
                listener.onCategoryClick(category, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final ItemCategoryBinding binding;

        CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
