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
import com.example.electronicsgadgets.databinding.DialogCheckoutBinding;
import com.example.electronicsgadgets.firebase.FirebaseHelper;
import com.example.electronicsgadgets.models.CartItem;
import com.example.electronicsgadgets.models.Order;
import com.example.electronicsgadgets.models.OrderItem;
import com.example.electronicsgadgets.models.User;
import com.example.electronicsgadgets.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckoutBottomSheet extends BottomSheetDialogFragment {

    private DialogCheckoutBinding binding;
    private SessionManager sessionManager;
    private FirebaseHelper firebaseHelper;
    private double subtotal = 0.0;
    private double deliveryFee = 0.0;
    private double totalAmount = 0.0;
    private List<CartItem> cartItems;

    public static CheckoutBottomSheet newInstance() {
        return new CheckoutBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        firebaseHelper = new FirebaseHelper(requireContext());

        calculateTotals();
        prefillUserData();

        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void calculateTotals() {
        cartItems = sessionManager.getCart();
        subtotal = 0.0;
        for (CartItem item : cartItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        deliveryFee = subtotal >= 1000.0 || subtotal == 0.0 ? 0.0 : 50.0;
        totalAmount = subtotal + deliveryFee;

        binding.tvCheckoutSubtotal.setText(String.format(Locale.US, "$%.2f", subtotal));
        binding.tvCheckoutDeliveryFee.setText(deliveryFee == 0 ? "Free" : String.format(Locale.US, "$%.2f", deliveryFee));
        binding.tvCheckoutTotal.setText(String.format(Locale.US, "$%.2f", totalAmount));
    }

    private void prefillUserData() {
        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getUserDetails();
            if (user != null) {
                if (!TextUtils.isEmpty(user.getName())) binding.etCheckoutName.setText(user.getName());
                if (!TextUtils.isEmpty(user.getPhone())) binding.etCheckoutPhone.setText(user.getPhone());
            }
        }
    }

    private void placeOrder() {
        String name = binding.etCheckoutName.getText() != null ? binding.etCheckoutName.getText().toString().trim() : "";
        String phone = binding.etCheckoutPhone.getText() != null ? binding.etCheckoutPhone.getText().toString().trim() : "";
        String address = binding.etCheckoutAddress.getText() != null ? binding.etCheckoutAddress.getText().toString().trim() : "";
        String city = binding.etCheckoutCity.getText() != null ? binding.etCheckoutCity.getText().toString().trim() : "";
        String state = binding.etCheckoutState.getText() != null ? binding.etCheckoutState.getText().toString().trim() : "";
        String pinCode = binding.etCheckoutPinCode.getText() != null ? binding.etCheckoutPinCode.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            binding.etCheckoutName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            binding.etCheckoutPhone.setError("Phone is required");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            binding.etCheckoutAddress.setError("Address is required");
            return;
        }
        if (TextUtils.isEmpty(city)) {
            binding.etCheckoutCity.setError("City is required");
            return;
        }
        if (TextUtils.isEmpty(state)) {
            binding.etCheckoutState.setError("State is required");
            return;
        }
        if (TextUtils.isEmpty(pinCode)) {
            binding.etCheckoutPinCode.setError("Pincode is required");
            return;
        }

        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cartItems) {
            orderItems.add(new OrderItem(
                    ci.getProductId(),
                    ci.getProductName(),
                    ci.getPrice(),
                    ci.getQuantity(),
                    ci.getImageUrl()
            ));
        }

        String orderId = "ORD-" + System.currentTimeMillis() % 100000;
        String userId = sessionManager.isLoggedIn() && sessionManager.getUserDetails() != null
                ? sessionManager.getUserDetails().getUid()
                : "guest_" + System.currentTimeMillis();

        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());

        Order order = new Order(
                orderId,
                userId,
                name,
                phone,
                address,
                city,
                state,
                pinCode,
                orderItems,
                subtotal,
                deliveryFee,
                totalAmount,
                currentDate,
                "Pending"
        );

        binding.btnPlaceOrder.setEnabled(false);
        binding.btnPlaceOrder.setText("Placing Order...");

        firebaseHelper.placeOrder(order, new FirebaseHelper.ActionCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Order Placed Successfully! #" + orderId, Toast.LENGTH_LONG).show();
                com.example.electronicsgadgets.utils.NotificationHelper.notifyOrderPlaced(requireContext(), order);
                sessionManager.clearCart();
                if (getActivity() instanceof MainActivity) {
                    MainActivity main = (MainActivity) getActivity();
                    main.updateCartBadge();
                    main.updateNotificationBadge();
                    main.navigateToOrders();
                }
                dismiss();
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Order Saved Locally: " + errorMessage, Toast.LENGTH_LONG).show();
                sessionManager.saveLocalOrder(order);
                com.example.electronicsgadgets.utils.NotificationHelper.notifyOrderPlaced(requireContext(), order);
                sessionManager.clearCart();
                if (getActivity() instanceof MainActivity) {
                    MainActivity main = (MainActivity) getActivity();
                    main.updateCartBadge();
                    main.updateNotificationBadge();
                    main.navigateToOrders();
                }
                dismiss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
