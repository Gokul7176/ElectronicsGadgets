package com.example.electronicsgadgets.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.electronicsgadgets.MainActivity;
import com.example.electronicsgadgets.adapters.NotificationAdapter;
import com.example.electronicsgadgets.databinding.ActivityNotificationsBinding;
import com.example.electronicsgadgets.firebase.FirebaseHelper;
import com.example.electronicsgadgets.models.NotificationModel;
import com.example.electronicsgadgets.models.User;
import com.example.electronicsgadgets.utils.NotificationHelper;
import com.example.electronicsgadgets.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private NotificationAdapter adapter;
    private String currentUserId = "user_demo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = new FirebaseHelper(this);
        sessionManager = new SessionManager(this);

        User user = sessionManager.getUserDetails();
        if (user != null) {
            currentUserId = user.getUid();
        }

        setupRecyclerView();
        setupClickListeners();
        loadNotifications();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this, new ArrayList<>(), notification -> {
            firebaseHelper.markNotificationAsRead(notification.getNotificationId(), null);
            notification.setRead(true);

            Intent intent = new Intent(NotificationsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            String target = "HOME";
            if ("cart".equalsIgnoreCase(notification.getType())) {
                target = "CART";
            } else if ("order".equalsIgnoreCase(notification.getType())) {
                target = "ORDERS";
            } else if ("product".equalsIgnoreCase(notification.getType())) {
                target = "PRODUCTS";
            }

            intent.putExtra(NotificationHelper.EXTRA_TARGET_FRAGMENT, target);
            if (notification.getOrderId() != null) {
                intent.putExtra(NotificationHelper.EXTRA_ORDER_ID, notification.getOrderId());
            }
            if (notification.getProductId() != null) {
                intent.putExtra(NotificationHelper.EXTRA_PRODUCT_ID, notification.getProductId());
            }
            startActivity(intent);
            finish();
        });

        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnMarkAllRead.setOnClickListener(v -> {
            firebaseHelper.markAllNotificationsAsRead(currentUserId, null);
            loadNotifications();
        });
    }

    private void loadNotifications() {
        firebaseHelper.fetchNotifications(currentUserId, new FirebaseHelper.NotificationsCallback() {
            @Override
            public void onSuccess(List<NotificationModel> notifications) {
                if (isFinishing() || binding == null) return;
                if (notifications == null || notifications.isEmpty()) {
                    binding.layoutEmptyNotifications.setVisibility(View.VISIBLE);
                    binding.rvNotifications.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyNotifications.setVisibility(View.GONE);
                    binding.rvNotifications.setVisibility(View.VISIBLE);
                    adapter.updateNotifications(notifications);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                if (isFinishing() || binding == null) return;
                List<NotificationModel> local = sessionManager.getLocalNotificationsForUser(currentUserId);
                if (local.isEmpty()) {
                    binding.layoutEmptyNotifications.setVisibility(View.VISIBLE);
                    binding.rvNotifications.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyNotifications.setVisibility(View.GONE);
                    binding.rvNotifications.setVisibility(View.VISIBLE);
                    adapter.updateNotifications(local);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}
