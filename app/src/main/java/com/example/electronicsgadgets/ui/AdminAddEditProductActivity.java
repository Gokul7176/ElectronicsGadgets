package com.example.electronicsgadgets.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.electronicsgadgets.databinding.ActivityAdminAddEditProductBinding;
import com.example.electronicsgadgets.firebase.FirebaseHelper;
import com.example.electronicsgadgets.models.Product;
import com.example.electronicsgadgets.models.User;
import com.example.electronicsgadgets.utils.NotificationHelper;
import com.example.electronicsgadgets.utils.SampleData;
import com.example.electronicsgadgets.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminAddEditProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product_to_edit";

    private ActivityAdminAddEditProductBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private Product existingProduct;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        firebaseHelper = new FirebaseHelper(this);

        // Security check: Only role="admin" allowed
        User currentUser = sessionManager.getUserDetails();
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            Toast.makeText(this, "Unauthorized access. Admin role required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getIntent() != null && getIntent().hasExtra(EXTRA_PRODUCT)) {
            existingProduct = (Product) getIntent().getSerializableExtra(EXTRA_PRODUCT);
            if (existingProduct != null) {
                isEditMode = true;
            }
        }

        setupCategoriesDropdown();
        setupToolbar();
        setupClickListeners();

        if (isEditMode) {
            prefillProductData();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Product" : "Add Product");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSave.setText(isEditMode ? "Update Product" : "Add Product");
    }

    private void setupCategoriesDropdown() {
        List<String> categories = new ArrayList<>();
        categories.add("Smartphones");
        categories.add("Laptops");
        categories.add("Audio");
        categories.add("Wearables");
        categories.add("Accessories");
        categories.add("Gaming");
        categories.add("Tablets");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        binding.actProductCategory.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveOrUpdateProduct());
    }

    private void prefillProductData() {
        if (existingProduct == null) return;

        binding.etProductName.setText(existingProduct.getName());
        binding.etProductBrand.setText(existingProduct.getBrand());
        binding.actProductCategory.setText(existingProduct.getCategory(), false);
        binding.etProductPrice.setText(String.format(Locale.US, "%.2f", existingProduct.getPrice()));
        binding.etProductOriginalPrice.setText(String.format(Locale.US, "%.2f", existingProduct.getOriginalPrice()));
        binding.etProductDiscount.setText(String.valueOf(existingProduct.getDiscount()));
        binding.etProductRating.setText(String.format(Locale.US, "%.1f", existingProduct.getRating()));
        binding.etProductStock.setText(String.valueOf(existingProduct.getStock()));
        binding.etProductImageUrl.setText(existingProduct.getImageUrl());
        binding.etProductDescription.setText(existingProduct.getDescription());
        binding.etProductSpecifications.setText(existingProduct.getSpecifications());
        binding.switchProductFeatured.setChecked(existingProduct.isFeatured());
    }

    private void saveOrUpdateProduct() {
        clearErrors();

        String name = binding.etProductName.getText() != null ? binding.etProductName.getText().toString().trim() : "";
        String brand = binding.etProductBrand.getText() != null ? binding.etProductBrand.getText().toString().trim() : "";
        String category = binding.actProductCategory.getText() != null ? binding.actProductCategory.getText().toString().trim() : "";
        String priceStr = binding.etProductPrice.getText() != null ? binding.etProductPrice.getText().toString().trim() : "";
        String origPriceStr = binding.etProductOriginalPrice.getText() != null ? binding.etProductOriginalPrice.getText().toString().trim() : "";
        String discountStr = binding.etProductDiscount.getText() != null ? binding.etProductDiscount.getText().toString().trim() : "";
        String ratingStr = binding.etProductRating.getText() != null ? binding.etProductRating.getText().toString().trim() : "";
        String stockStr = binding.etProductStock.getText() != null ? binding.etProductStock.getText().toString().trim() : "";
        String imageUrl = binding.etProductImageUrl.getText() != null ? binding.etProductImageUrl.getText().toString().trim() : "";
        String description = binding.etProductDescription.getText() != null ? binding.etProductDescription.getText().toString().trim() : "";
        String specs = binding.etProductSpecifications.getText() != null ? binding.etProductSpecifications.getText().toString().trim() : "";
        boolean isFeatured = binding.switchProductFeatured.isChecked();

        // 1. Validation
        if (TextUtils.isEmpty(name)) {
            binding.tilProductName.setError("Product name cannot be empty");
            binding.etProductName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(brand)) {
            binding.tilProductBrand.setError("Brand cannot be empty");
            binding.etProductBrand.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(category)) {
            binding.tilProductCategory.setError("Category must be selected");
            binding.actProductCategory.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            binding.tilProductPrice.setError("Price must be greater than 0");
            binding.etProductPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                binding.tilProductPrice.setError("Price must be greater than 0");
                binding.etProductPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            binding.tilProductPrice.setError("Invalid price format");
            binding.etProductPrice.requestFocus();
            return;
        }

        double origPrice = price;
        if (!TextUtils.isEmpty(origPriceStr)) {
            try {
                origPrice = Double.parseDouble(origPriceStr);
                if (origPrice < price) {
                    binding.tilProductOriginalPrice.setError("Original price must be greater than or equal to price");
                    binding.etProductOriginalPrice.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                binding.tilProductOriginalPrice.setError("Invalid original price format");
                binding.etProductOriginalPrice.requestFocus();
                return;
            }
        }

        float rating = 4.5f;
        if (!TextUtils.isEmpty(ratingStr)) {
            try {
                rating = Float.parseFloat(ratingStr);
                if (rating < 0 || rating > 5) {
                    binding.tilProductRating.setError("Rating must be between 0 and 5");
                    binding.etProductRating.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                binding.tilProductRating.setError("Rating must be between 0 and 5");
                binding.etProductRating.requestFocus();
                return;
            }
        }

        int stock = 10;
        if (!TextUtils.isEmpty(stockStr)) {
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    binding.tilProductStock.setError("Stock cannot be negative");
                    binding.etProductStock.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                binding.tilProductStock.setError("Stock cannot be negative");
                binding.etProductStock.requestFocus();
                return;
            }
        }

        if (TextUtils.isEmpty(description)) {
            binding.tilProductDescription.setError("Description cannot be empty");
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

        String productId;
        if (isEditMode && existingProduct != null) {
            productId = existingProduct.getProductId();
        } else {
            // Generate UNIQUE productId
            if (firebaseHelper.isFirebaseAvailable()) {
                productId = FirebaseFirestore.getInstance().collection("products").document().getId();
            } else {
                productId = "prod_" + System.currentTimeMillis();
            }
        }

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

        binding.btnSave.setEnabled(false);
        binding.btnSave.setText(isEditMode ? "Updating..." : "Adding...");

        FirebaseHelper.ActionCallback callback = new FirebaseHelper.ActionCallback() {
            @Override
            public void onSuccess() {
                if (isFinishing()) return;
                if (isEditMode) {
                    Toast.makeText(AdminAddEditProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminAddEditProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    NotificationHelper.notifyNewProduct(AdminAddEditProductActivity.this, product);
                }
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                if (isFinishing()) return;
                Toast.makeText(AdminAddEditProductActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                binding.btnSave.setEnabled(true);
                binding.btnSave.setText(isEditMode ? "Update Product" : "Add Product");
            }
        };

        if (isEditMode) {
            firebaseHelper.updateProduct(product, callback);
        } else {
            firebaseHelper.createProduct(product, callback);
        }
    }

    private void clearErrors() {
        binding.tilProductName.setError(null);
        binding.tilProductBrand.setError(null);
        binding.tilProductCategory.setError(null);
        binding.tilProductPrice.setError(null);
        binding.tilProductOriginalPrice.setError(null);
        binding.tilProductDiscount.setError(null);
        binding.tilProductRating.setError(null);
        binding.tilProductStock.setError(null);
        binding.tilProductDescription.setError(null);
        binding.tilProductSpecifications.setError(null);
        binding.tilProductImageUrl.setError(null);
    }
}
