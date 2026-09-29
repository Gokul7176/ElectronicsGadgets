package com.example.electronicsgadgets.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.electronicsgadgets.MainActivity;
import com.example.electronicsgadgets.adapters.CategoryAdapter;
import com.example.electronicsgadgets.adapters.ProductAdapter;
import com.example.electronicsgadgets.databinding.FragmentHomeBinding;
import com.example.electronicsgadgets.firebase.FirebaseHelper;
import com.example.electronicsgadgets.models.CartItem;
import com.example.electronicsgadgets.models.Category;
import com.example.electronicsgadgets.models.Product;
import com.example.electronicsgadgets.utils.SampleData;
import com.example.electronicsgadgets.utils.SessionManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseHelper = new FirebaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupCategoryRecyclerView();
        setupProductRecyclerView();
        setupClickListeners();
        loadProducts();
    }

    private void setupCategoryRecyclerView() {
        List<Category> categories = SampleData.getCategories();
        categoryAdapter = new CategoryAdapter(requireContext(), categories, (category, position) -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToProductsCategory(category.getName());
            }
        });
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupProductRecyclerView() {
        productAdapter = new ProductAdapter(requireContext(), new ArrayList<>(), new ProductAdapter.OnProductClickListener() {
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

        binding.rvFeaturedProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvFeaturedProducts.setAdapter(productAdapter);
    }

    private void setupClickListeners() {
        binding.cardHomeSearch.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToProducts();
            }
        });

        binding.btnSeeAllCategories.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToProducts();
            }
        });

        binding.btnSeeAllProducts.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToProducts();
            }
        });
    }

    public void loadProducts() {
        firebaseHelper.fetchProducts(new FirebaseHelper.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (!isAdded() || binding == null) return;
                List<Product> featured = new ArrayList<>();
                if (products != null) {
                    for (Product p : products) {
                        if (p.isFeatured()) {
                            featured.add(p);
                        }
                    }
                    if (featured.isEmpty()) {
                        featured = products.size() > 6 ? products.subList(0, 6) : products;
                    }
                }
                productAdapter.updateProducts(featured);
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded() || binding == null) return;
                productAdapter.updateProducts(sessionManager.getLocalProducts());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts();
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
