package com.example.electronicsgadgets.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.electronicsgadgets.R;
import com.example.electronicsgadgets.adapters.AdminProductAdapter;
import com.example.electronicsgadgets.adapters.CategoryAdapter;
import com.example.electronicsgadgets.databinding.ActivityAdminProductBinding;
import com.example.electronicsgadgets.firebase.FirebaseHelper;
import com.example.electronicsgadgets.models.Category;
import com.example.electronicsgadgets.models.Product;
import com.example.electronicsgadgets.models.User;
import com.example.electronicsgadgets.utils.SampleData;
import com.example.electronicsgadgets.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminProductActivity extends AppCompatActivity {

    private ActivityAdminProductBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private AdminProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    private String selectedCategory = "All";
    private String searchQuery = "";
    private int currentSortId = R.id.chipAll;

    private ActivityResultLauncher<Intent> addEditLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        firebaseHelper = new FirebaseHelper(this);

        // Security Check: Only admin user permitted
        User user = sessionManager.getUserDetails();
        if (user == null || !"admin".equalsIgnoreCase(user.getRole())) {
            Toast.makeText(this, "Unauthorized access. Admin role required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        addEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadProducts();
                    }
                }
        );

        setupToolbar();
        setupCategoryFilters();
        setupProductRecyclerView();
        setupSearchAndSort();
        setupClickListeners();

        loadProducts();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Products");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupCategoryFilters() {
        List<Category> categories = SampleData.getCategories();
        categoryAdapter = new CategoryAdapter(this, categories, (category, position) -> {
            selectedCategory = category.getName();
            applyFilters();
        });

        binding.rvCategoryFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategoryFilters.setAdapter(categoryAdapter);
        categoryAdapter.setSelectedCategoryById(selectedCategory);
    }

    private void setupProductRecyclerView() {
        productAdapter = new AdminProductAdapter(this, filteredProducts, new AdminProductAdapter.OnAdminProductActionListener() {
            @Override
            public void onEditClick(Product product) {
                Intent intent = new Intent(AdminProductActivity.this, AdminAddEditProductActivity.class);
                intent.putExtra(AdminAddEditProductActivity.EXTRA_PRODUCT, product);
                addEditLauncher.launch(intent);
            }

            @Override
            public void onDeleteClick(Product product) {
                showDeleteConfirmationDialog(product);
            }
        });

        binding.rvAdminProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminProducts.setAdapter(productAdapter);
    }

    private void setupSearchAndSort() {
        binding.etSearchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString().trim().toLowerCase() : "";
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                currentSortId = checkedIds.get(0);
                applyFilters();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnAddProduct.setOnClickListener(v -> openAddProductScreen());
        binding.btnEmptyAddProduct.setOnClickListener(v -> openAddProductScreen());
    }

    private void openAddProductScreen() {
        Intent intent = new Intent(AdminProductActivity.this, AdminAddEditProductActivity.class);
        addEditLauncher.launch(intent);
    }

    private void loadProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        firebaseHelper.getAllProducts(new FirebaseHelper.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                binding.progressBar.setVisibility(View.GONE);
                allProducts = products != null ? products : new ArrayList<>();
                applyFilters();
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.progressBar.setVisibility(View.GONE);
                allProducts = sessionManager.getLocalProducts();
                applyFilters();
                Toast.makeText(AdminProductActivity.this, "Offline mode — showing saved products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        filteredProducts = new ArrayList<>();

        for (Product p : allProducts) {
            boolean matchesCategory = selectedCategory.equalsIgnoreCase("All")
                    || p.getCategory().equalsIgnoreCase(selectedCategory);

            boolean matchesSearch = searchQuery.isEmpty()
                    || (p.getName() != null && p.getName().toLowerCase().contains(searchQuery))
                    || (p.getBrand() != null && p.getBrand().toLowerCase().contains(searchQuery))
                    || (p.getCategory() != null && p.getCategory().toLowerCase().contains(searchQuery));

            if (matchesCategory && matchesSearch) {
                filteredProducts.add(p);
            }
        }

        // Apply Sorting
        if (currentSortId == R.id.chipPriceLowHigh) {
            Collections.sort(filteredProducts, Comparator.comparingDouble(Product::getPrice));
        } else if (currentSortId == R.id.chipPriceHighLow) {
            Collections.sort(filteredProducts, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        } else if (currentSortId == R.id.chipRating) {
            Collections.sort(filteredProducts, (p1, p2) -> Float.compare(p2.getRating(), p1.getRating()));
        }

        productAdapter.updateProducts(filteredProducts);

        if (filteredProducts.isEmpty()) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.rvAdminProducts.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.rvAdminProducts.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteConfirmationDialog(Product product) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Product?")
                .setMessage("Are you sure you want to delete " + product.getName() + "?")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteProduct(product);
                })
                .show();
    }

    private void deleteProduct(Product product) {
        binding.progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.deleteProduct(product.getProductId(), new FirebaseHelper.ActionCallback() {
            @Override
            public void onSuccess() {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminProductActivity.this, "Product deleted successfully.", Toast.LENGTH_SHORT).show();
                loadProducts();
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminProductActivity.this, "Error deleting product: " + errorMessage, Toast.LENGTH_LONG).show();
                loadProducts();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
}
