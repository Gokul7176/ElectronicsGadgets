package com.example.electronicsgadgets;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.electronicsgadgets.databinding.ActivityMainBinding;
import com.example.electronicsgadgets.firebase.FirebaseHelper;
import com.example.electronicsgadgets.models.CartItem;
import com.example.electronicsgadgets.models.NotificationModel;
import com.example.electronicsgadgets.models.Product;
import com.example.electronicsgadgets.models.User;
import com.example.electronicsgadgets.ui.CartFragment;
import com.example.electronicsgadgets.ui.HomeFragment;
import com.example.electronicsgadgets.ui.NotificationsActivity;
import com.example.electronicsgadgets.ui.OrdersFragment;
import com.example.electronicsgadgets.ui.ProductDetailBottomSheet;
import com.example.electronicsgadgets.ui.ProductsFragment;
import com.example.electronicsgadgets.ui.ProfileFragment;
import com.example.electronicsgadgets.utils.NotificationHelper;
import com.example.electronicsgadgets.utils.SessionManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private FirebaseHelper firebaseHelper;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        firebaseHelper = new FirebaseHelper(this);

        setupNotificationPermission();
        setupBottomNavigation();
        setupHeaderActions();
        updateCartBadge();
        updateNotificationBadge();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "HOME_FRAGMENT");
            handleNotificationIntent(getIntent());
        }
    }

    private void setupNotificationPermission() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    // Permission result handled silently
                }
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment(), "HOME_FRAGMENT");
                return true;
            } else if (itemId == R.id.nav_products) {
                loadFragment(ProductsFragment.newInstance("All"), "PRODUCTS_FRAGMENT");
                return true;
            } else if (itemId == R.id.nav_cart) {
                loadFragment(new CartFragment(), "CART_FRAGMENT");
                return true;
            } else if (itemId == R.id.nav_orders) {
                loadFragment(new OrdersFragment(), "ORDERS_FRAGMENT");
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment(), "PROFILE_FRAGMENT");
                return true;
            }
            return false;
        });
    }

    private void setupHeaderActions() {
        binding.btnHeaderCart.setOnClickListener(v -> {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_cart);
        });

        binding.btnHeaderNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });
    }

    public void updateCartBadge() {
        if (binding == null) return;
        List<CartItem> cart = sessionManager.getCart();
        int totalCount = 0;
        if (cart != null) {
            for (CartItem item : cart) {
                totalCount += item.getQuantity();
            }
        }

        if (totalCount > 0) {
            binding.tvCartBadge.setText(String.valueOf(totalCount));
            binding.tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            binding.tvCartBadge.setVisibility(View.GONE);
        }
    }

    public void updateNotificationBadge() {
        if (binding == null) return;
        User user = sessionManager.getUserDetails();
        String userId = user != null ? user.getUid() : "user_demo";

        firebaseHelper.fetchNotifications(userId, new FirebaseHelper.NotificationsCallback() {
            @Override
            public void onSuccess(List<NotificationModel> notifications) {
                if (binding == null) return;
                int unreadCount = 0;
                if (notifications != null) {
                    for (NotificationModel n : notifications) {
                        if (!n.isRead()) unreadCount++;
                    }
                }
                if (unreadCount > 0) {
                    binding.tvNotificationBadge.setText(String.valueOf(unreadCount));
                    binding.tvNotificationBadge.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNotificationBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                if (binding == null) return;
                int unreadCount = sessionManager.getUnreadNotificationCount(userId);
                if (unreadCount > 0) {
                    binding.tvNotificationBadge.setText(String.valueOf(unreadCount));
                    binding.tvNotificationBadge.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNotificationBadge.setVisibility(View.GONE);
                }
            }
        });
    }

    public void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment, tag)
                .commit();
    }

    public void navigateToProducts() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_products);
    }

    public void navigateToProductsCategory(String categoryName) {
        loadFragment(ProductsFragment.newInstance(categoryName), "PRODUCTS_FRAGMENT");
        binding.bottomNavigation.getMenu().findItem(R.id.nav_products).setChecked(true);
    }

    public void navigateToOrders() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_orders);
    }

    public void refreshCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof ProfileFragment) {
            ((ProfileFragment) currentFragment).updateUI();
        } else if (currentFragment instanceof CartFragment) {
            ((CartFragment) currentFragment).loadCartData();
        } else if (currentFragment instanceof OrdersFragment) {
            ((OrdersFragment) currentFragment).loadOrders();
        } else if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).loadProducts();
        } else if (currentFragment instanceof ProductsFragment) {
            ((ProductsFragment) currentFragment).loadProducts();
        }
    }

    public void refreshProductCatalog() {
        refreshCurrentFragment();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent == null || !intent.hasExtra(NotificationHelper.EXTRA_TARGET_FRAGMENT)) return;

        String target = intent.getStringExtra(NotificationHelper.EXTRA_TARGET_FRAGMENT);
        String notifId = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_ID);
        String productId = intent.getStringExtra(NotificationHelper.EXTRA_PRODUCT_ID);

        if (notifId != null) {
            firebaseHelper.markNotificationAsRead(notifId, null);
        }

        if ("CART".equalsIgnoreCase(target)) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_cart);
        } else if ("ORDERS".equalsIgnoreCase(target)) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_orders);
        } else if ("PRODUCTS".equalsIgnoreCase(target)) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_products);
        } else if ("HOME".equalsIgnoreCase(target)) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        if (productId != null && !productId.isEmpty()) {
            firebaseHelper.fetchProducts(new FirebaseHelper.ProductsCallback() {
                @Override
                public void onSuccess(List<Product> products) {
                    for (Product p : products) {
                        if (p.getProductId().equals(productId)) {
                            ProductDetailBottomSheet.newInstance(p).show(getSupportFragmentManager(), "product_detail");
                            break;
                        }
                    }
                }

                @Override
                public void onFailure(String errorMessage) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        updateNotificationBadge();
    }
}