package com.example.electronicsgadgets.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.electronicsgadgets.MainActivity;
import com.example.electronicsgadgets.adapters.CartAdapter;
import com.example.electronicsgadgets.databinding.FragmentCartBinding;
import com.example.electronicsgadgets.models.CartItem;
import com.example.electronicsgadgets.utils.SessionManager;

import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartAdapter cartAdapter;
    private SessionManager sessionManager;
    private List<CartItem> cartItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        setupCartRecyclerView();
        setupClickListeners();
        loadCartData();
    }

    private void setupCartRecyclerView() {
        cartAdapter = new CartAdapter(requireContext(), cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged(CartItem cartItem, int newQuantity) {
                cartItem.setQuantity(newQuantity);
                sessionManager.saveCart(cartItems);
                loadCartData();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateCartBadge();
                }
            }

            @Override
            public void onItemRemoved(CartItem cartItem) {
                cartItems.remove(cartItem);
                sessionManager.saveCart(cartItems);
                loadCartData();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateCartBadge();
                }
            }
        });

        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCartItems.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        binding.btnContinueShopping.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToProducts();
            }
        });

        binding.btnProceedCheckout.setOnClickListener(v -> {
            if (cartItems != null && !cartItems.isEmpty()) {
                CheckoutBottomSheet.newInstance().show(getParentFragmentManager(), "checkout_bottom_sheet");
            }
        });
    }

    public void loadCartData() {
        cartItems = sessionManager.getCart();

        if (cartItems == null || cartItems.isEmpty()) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.layoutCartContent.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.layoutCartContent.setVisibility(View.VISIBLE);

            cartAdapter.updateCartItems(cartItems);

            double subtotal = 0.0;
            for (CartItem item : cartItems) {
                subtotal += item.getPrice() * item.getQuantity();
            }

            double deliveryFee = subtotal >= 1000.0 ? 0.0 : 50.0;
            double total = subtotal + deliveryFee;

            binding.tvCartSubtotal.setText(String.format(Locale.US, "$%.2f", subtotal));
            binding.tvCartDeliveryFee.setText(deliveryFee == 0 ? "Free" : String.format(Locale.US, "$%.2f", deliveryFee));
            binding.tvCartTotal.setText(String.format(Locale.US, "$%.2f", total));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCartData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
