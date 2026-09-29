package com.example.electronicsgadgets.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.electronicsgadgets.MainActivity;
import com.example.electronicsgadgets.databinding.DialogAddProductBinding;
import com.example.electronicsgadgets.firebase.FirebaseHelper;
import com.example.electronicsgadgets.models.Product;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

public class AddProductBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCT = "arg_product_edit";
    private DialogAddProductBinding binding;
    private FirebaseHelper firebaseHelper;
    private Product existingProduct;

    public static AddProductBottomSheet newInstance(@Nullable Product productToEdit) {
        AddProductBottomSheet fragment = new AddProductBottomSheet();
        if (productToEdit != null) {
            Bundle args = new Bundle();
            args.putSerializable(ARG_PRODUCT, productToEdit);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            existingProduct = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseHelper = new FirebaseHelper(requireContext());

        if (existingProduct != null) {
            binding.tvAddProductTitle.setText("Edit Product");
            prefillProductData();
        }

        binding.btnCancelProduct.setOnClickListener(v -> dismiss());
        binding.btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void prefillProductData() {
        binding.etProductName.setText(existingProduct.getName());
        binding.etProductBrand.setText(existingProduct.getBrand());
        binding.etProductCategory.setText(existingProduct.getCategory());
        binding.etProductPrice.setText(String.format(Locale.US, "%.2f", existingProduct.getPrice()));
        binding.etProductOriginalPrice.setText(String.format(Locale.US, "%.2f", existingProduct.getOriginalPrice()));
        binding.etProductStock.setText(String.valueOf(existingProduct.getStock()));
        binding.etProductDiscount.setText(String.valueOf(existingProduct.getDiscount()));
        binding.etProductRating.setText(String.format(Locale.US, "%.1f", existingProduct.getRating()));
        binding.etProductImageUrl.setText(existingProduct.getImageUrl());
        binding.etProductDescription.setText(existingProduct.getDescription());
        binding.etProductSpecifications.setText(existingProduct.getSpecifications());
        binding.cbProductFeatured.setChecked(existingProduct.isFeatured());
    }

    private void saveProduct() {
        String name = binding.etProductName.getText() != null ? binding.etProductName.getText().toString().trim() : "";
        String brand = binding.etProductBrand.getText() != null ? binding.etProductBrand.getText().toString().trim() : "";
        String category = binding.etProductCategory.getText() != null ? binding.etProductCategory.getText().toString().trim() : "";
        String priceStr = binding.etProductPrice.getText() != null ? binding.etProductPrice.getText().toString().trim() : "";
        String origPriceStr = binding.etProductOriginalPrice.getText() != null ? binding.etProductOriginalPrice.getText().toString().trim() : "";
        String stockStr = binding.etProductStock.getText() != null ? binding.etProductStock.getText().toString().trim() : "";
        String discountStr = binding.etProductDiscount.getText() != null ? binding.etProductDiscount.getText().toString().trim() : "";
        String ratingStr = binding.etProductRating.getText() != null ? binding.etProductRating.getText().toString().trim() : "";
        String imageUrl = binding.etProductImageUrl.getText() != null ? binding.etProductImageUrl.getText().toString().trim() : "";
        String description = binding.etProductDescription.getText() != null ? binding.etProductDescription.getText().toString().trim() : "";
        String specs = binding.etProductSpecifications.getText() != null ? binding.etProductSpecifications.getText().toString().trim() : "";
        boolean isFeatured = binding.cbProductFeatured.isChecked();

        if (TextUtils.isEmpty(name)) {
            binding.etProductName.setError("Product name cannot be empty");
            binding.etProductName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(brand)) {
            binding.etProductBrand.setError("Brand cannot be empty");
            binding.etProductBrand.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(category)) {
            binding.etProductCategory.setError("Category must be selected");
            binding.etProductCategory.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            binding.etProductPrice.setError("Price must be greater than 0");
            binding.etProductPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                binding.etProductPrice.setError("Price must be greater than 0");
                binding.etProductPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            binding.etProductPrice.setError("Invalid price format");
            binding.etProductPrice.requestFocus();
            return;
        }

        double origPrice = price;
        if (!TextUtils.isEmpty(origPriceStr)) {
            try {
                origPrice = Double.parseDouble(origPriceStr);
                if (origPrice < price) {
                    binding.etProductOriginalPrice.setError("Original price must be greater than or equal to price");
                    binding.etProductOriginalPrice.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                binding.etProductOriginalPrice.setError("Invalid original price format");
                binding.etProductOriginalPrice.requestFocus();
                return;
            }
        }

        float rating = 4.5f;
        if (!TextUtils.isEmpty(ratingStr)) {
            try {
                rating = Float.parseFloat(ratingStr);
                if (rating < 0 || rating > 5) {
                    binding.etProductRating.setError("Rating must be between 0 and 5");
                    binding.etProductRating.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                binding.etProductRating.setError("Rating must be between 0 and 5");
                binding.etProductRating.requestFocus();
                return;
            }
        }

        int stock = 10;
        if (!TextUtils.isEmpty(stockStr)) {
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    binding.etProductStock.setError("Stock cannot be negative");
                    binding.etProductStock.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                binding.etProductStock.setError("Stock cannot be negative");
                binding.etProductStock.requestFocus();
                return;
            }
        }

        if (TextUtils.isEmpty(description)) {
            binding.etProductDescription.setError("Description cannot be empty");
            binding.etProductDescription.requestFocus();
            return;
        }

        int discount = 0;
        if (!TextUtils.isEmpty(discountStr)) {
            try {
                discount = Integer.parseInt(discountStr);
            } catch (NumberFormatException ignored) {}
        } else if (origPrice > price) {
            discount = (int) Math.round(((origPrice - price) / origPrice) * 100);
        }

        if (TextUtils.isEmpty(imageUrl)) {
            imageUrl = "https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=500";
        }

        String productId = existingProduct != null ? existingProduct.getProductId() : "prod_" + System.currentTimeMillis();

        Product product = new Product(
                productId,
                name,
                brand,
                category,
                price,
                origPrice,
                discount,
                rating,
                description,
                specs,
                imageUrl,
                stock,
                isFeatured
        );

        binding.btnSaveProduct.setEnabled(false);
        binding.btnSaveProduct.setText("Saving...");

        firebaseHelper.saveProduct(product, new FirebaseHelper.ActionCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), existingProduct != null ? "Product updated successfully" : "Product added successfully", Toast.LENGTH_SHORT).show();
                if (existingProduct == null) {
                    com.example.electronicsgadgets.utils.NotificationHelper.notifyNewProduct(requireContext(), product);
                }
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).refreshCurrentFragment();
                    ((MainActivity) getActivity()).updateNotificationBadge();
                }
                dismiss();
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error saving product: " + errorMessage, Toast.LENGTH_LONG).show();
                binding.btnSaveProduct.setEnabled(true);
                binding.btnSaveProduct.setText("Save Product");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
