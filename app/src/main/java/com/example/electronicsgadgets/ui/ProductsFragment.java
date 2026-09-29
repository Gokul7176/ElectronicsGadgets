package com.example.electronicsgadgets.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.electronicsgadgets.MainActivity;
import com.example.electronicsgadgets.R;
import com.example.electronicsgadgets.adapters.CategoryAdapter;
import com.example.electronicsgadgets.adapters.ProductAdapter;
import com.example.electronicsgadgets.databinding.FragmentProductsBinding;
import com.example.electronicsgadgets.firebase.FirebaseHelper;
import com.example.electronicsgadgets.models.CartItem;
import com.example.electronicsgadgets.models.Category;
import com.example.electronicsgadgets.models.Product;
import com.example.electronicsgadgets.utils.SampleData;
import com.example.electronicsgadgets.utils.SessionManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductsFragment extends Fragment {

    private static final String ARG_CATEGORY = "arg_initial_category";
    private FragmentProductsBinding binding;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    private String selectedCategory = "All";
    private String searchQuery = "";
    private int currentSortId = R.id.chipRecommended;

    public static ProductsFragment newInstance(@Nullable String initialCategory) {
        ProductsFragment fragment = new ProductsFragment();
        if (initialCategory != null) {
            Bundle args = new Bundle();
            args.putString(ARG_CATEGORY, initialCategory);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedCategory = getArguments().getString(ARG_CATEGORY, "All");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseHelper = new FirebaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupCategoryChips();
        setupProductGrid();
        setupSearchAndSort();
        loadProducts();
    }

    private void setupCategoryChips() {
        List<Category> categories = SampleData.getCategories();
        categoryAdapter = new CategoryAdapter(requireContext(), categories, (category, position) -> {
            selectedCategory = category.getName();
            applyFilters();
        });

        binding.rvProductCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvProductCategories.setAdapter(categoryAdapter);
        categoryAdapter.setSelectedCategoryById(selectedCategory);
    }

    private void setupProductGrid() {
        productAdapter = new ProductAdapter(requireContext(), filteredProducts, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                ProductDetailBottomSheet.newInstance(product).show(getParentFragmentManager(), "product_detail");
            }

            @Override
            public void onAddToCartClick(Product product) {
                addToCart(product);
                com.example.electronicsgadgets.utils.NotificationHelper.notifyAddToCart(requireContext(), product);
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateCartBadge();
                    ((MainActivity) getActivity()).updateNotificationBadge();
                }
                if (getView() != null) {
                    Snackbar.make(getView(), product.getName() + " added to cart", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        binding.rvProductsGrid.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvProductsGrid.setAdapter(productAdapter);
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

    public void loadProducts() {
        firebaseHelper.fetchProducts(new FirebaseHelper.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (!isAdded() || binding == null) return;
                allProducts = products != null ? products : new ArrayList<>();
                applyFilters();
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded() || binding == null) return;
                allProducts = sessionManager.getLocalProducts();
                applyFilters();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts();
    }

    private void applyFilters() {
        filteredProducts = new ArrayList<>();

        for (Product p : allProducts) {
            boolean matchesCategory = selectedCategory.equalsIgnoreCase("All")
                    || p.getCategory().equalsIgnoreCase(selectedCategory);

            boolean matchesSearch = searchQuery.isEmpty()
                    || p.getName().toLowerCase().contains(searchQuery)
                    || p.getBrand().toLowerCase().contains(searchQuery)
                    || p.getCategory().toLowerCase().contains(searchQuery);

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
            binding.layoutEmptyProducts.setVisibility(View.VISIBLE);
            binding.rvProductsGrid.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyProducts.setVisibility(View.GONE);
            binding.rvProductsGrid.setVisibility(View.VISIBLE);
        }
    }

    private void addToCart(Product product) {
        List<CartItem> cart = sessionManager.getCart();
        boolean existing = false;
        for (CartItem item : cart) {
            if (item.getProductId().equals(product.getProductId())) {
                item.setQuantity(Math.min(item.getQuantity() + 1, product.getStock()));
                existing = true;
                break;
            }
        }
        if (!existing) {
            cart.add(new CartItem(
                    "cart_" + System.currentTimeMillis(),
                    product.getProductId(),
                    product.getName(),
                    product.getBrand(),
                    product.getPrice(),
                    product.getImageUrl(),
                    1,
                    product.getStock()
            ));
        }
        sessionManager.saveCart(cart);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
