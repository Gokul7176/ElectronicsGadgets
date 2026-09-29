package com.example.electronicsgadgets.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.electronicsgadgets.MainActivity;
import com.example.electronicsgadgets.adapters.OrderAdapter;
import com.example.electronicsgadgets.databinding.FragmentOrdersBinding;
import com.example.electronicsgadgets.firebase.FirebaseHelper;
import com.example.electronicsgadgets.models.Order;
import com.example.electronicsgadgets.models.User;
import com.example.electronicsgadgets.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private OrderAdapter orderAdapter;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private List<Order> orderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseHelper = new FirebaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupOrderRecyclerView();
        setupListeners();
        loadOrders();
    }

    private void setupOrderRecyclerView() {
        orderAdapter = new OrderAdapter(requireContext(), orderList, order -> {
            // Admin status update option
            User currentUser = sessionManager.getUserDetails();
            if (currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole())) {
                showAdminStatusDialog(order);
            }
        });

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrders.setAdapter(orderAdapter);
    }

    private void setupListeners() {
        binding.swipeRefreshOrders.setOnRefreshListener(this::loadOrders);

        binding.btnStartShopping.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToProducts();
            }
        });
    }

    public void loadOrders() {
        binding.swipeRefreshOrders.setRefreshing(true);
        User user = sessionManager.getUserDetails();

        if (user != null && "admin".equalsIgnoreCase(user.getRole())) {
            firebaseHelper.fetchAllOrders(new FirebaseHelper.OrdersCallback() {
                @Override
                public void onSuccess(List<Order> orders) {
                    onOrdersLoaded(orders);
                }

                @Override
                public void onFailure(String errorMessage) {
                    onOrdersLoaded(sessionManager.getLocalOrders());
                }
            });
        } else {
            String userId = user != null ? user.getUid() : "guest";
            firebaseHelper.fetchUserOrders(userId, new FirebaseHelper.OrdersCallback() {
                @Override
                public void onSuccess(List<Order> orders) {
                    onOrdersLoaded(orders);
                }

                @Override
                public void onFailure(String errorMessage) {
                    onOrdersLoaded(sessionManager.getLocalOrders());
                }
            });
        }
    }

    private void onOrdersLoaded(List<Order> orders) {
        if (!isAdded() || binding == null) return;
        binding.swipeRefreshOrders.setRefreshing(false);
        this.orderList = orders != null ? orders : new ArrayList<>();

        if (orderList.isEmpty()) {
            binding.layoutEmptyOrders.setVisibility(View.VISIBLE);
            binding.rvOrders.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyOrders.setVisibility(View.GONE);
            binding.rvOrders.setVisibility(View.VISIBLE);
            orderAdapter.updateOrders(orderList);
        }
    }

    private void showAdminStatusDialog(Order order) {
        String[] statuses = {"Pending", "Confirmed", "Shipped", "Delivered", "Cancelled"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Update Status: #" + order.getOrderId())
                .setItems(statuses, (dialog, which) -> {
                    String selectedStatus = statuses[which];
                    order.setStatus(selectedStatus);
                    com.example.electronicsgadgets.utils.NotificationHelper.notifyOrderStatusChanged(requireContext(), order, selectedStatus);

                    firebaseHelper.updateOrderStatus(order.getOrderId(), selectedStatus, new FirebaseHelper.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(requireContext(), "Order status updated to " + selectedStatus, Toast.LENGTH_SHORT).show();
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).updateNotificationBadge();
                            }
                            loadOrders();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(requireContext(), "Updated status locally: " + selectedStatus, Toast.LENGTH_SHORT).show();
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).updateNotificationBadge();
                            }
                            loadOrders();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
