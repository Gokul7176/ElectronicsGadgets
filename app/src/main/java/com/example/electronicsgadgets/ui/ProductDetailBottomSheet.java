package com.example.electronicsgadgets.ui;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.electronicsgadgets.MainActivity;
import com.example.electronicsgadgets.R;
import com.example.electronicsgadgets.databinding.DialogProductDetailBinding;
import com.example.electronicsgadgets.models.CartItem;
import com.example.electronicsgadgets.models.Product;
import com.example.electronicsgadgets.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

public class ProductDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCT = "arg_product";
    private DialogProductDetailBinding binding;
    private Product product;
    private int quantity = 1;

    public static ProductDetailBottomSheet newInstance(Product product) {
        ProductDetailBottomSheet fragment = new ProductDetailBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (product == null) {
            dismiss();
            return;
        }

        bindProductData();
        setupListeners();
    }

    private void bindProductData() {
        binding.tvDetailName.setText(product.getName());
        binding.tvDetailBrand.setText(product.getBrand());
        binding.tvDetailRating.setText(String.format(Locale.US, "%.1f", product.getRating()));
        binding.tvDetailPrice.setText(String.format(Locale.US, "$%.2f", product.getPrice()));
        binding.tvDetailDescription.setText(product.getDescription());
        binding.tvDetailSpecifications.setText(product.getSpecifications());
        binding.tvDetailStock.setText(String.format(Locale.US, "In Stock (%d left)", product.getStock()));

        if (product.getOriginalPrice() > product.getPrice()) {
            binding.tvDetailOriginalPrice.setVisibility(View.VISIBLE);
            binding.tvDetailOriginalPrice.setText(String.format(Locale.US, "$%.2f", product.getOriginalPrice()));
            binding.tvDetailOriginalPrice.setPaintFlags(binding.tvDetailOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            binding.tvDetailOriginalPrice.setVisibility(View.GONE);
        }

        if (product.getDiscount() > 0) {
            binding.tvDetailDiscount.setVisibility(View.VISIBLE);
            binding.tvDetailDiscount.setText(String.format(Locale.US, "-%d%%", product.getDiscount()));
        } else {
            binding.tvDetailDiscount.setVisibility(View.GONE);
        }

        Glide.with(requireContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_placeholder_product)
                .error(R.drawable.ic_placeholder_product)
                .into(binding.imgDetailProduct);

        binding.tvDetailQuantity.setText(String.valueOf(quantity));
    }

    private void setupListeners() {
        binding.btnDetailPlus.setOnClickListener(v -> {
            if (quantity < product.getStock()) {
                quantity++;
                binding.tvDetailQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(requireContext(), "Cannot exceed available stock", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnDetailMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvDetailQuantity.setText(String.valueOf(quantity));
            }
        });

        binding.btnDetailAddToCart.setOnClickListener(v -> {
            addToCart();
            com.example.electronicsgadgets.utils.NotificationHelper.notifyAddToCart(requireContext(), product);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateCartBadge();
                ((MainActivity) getActivity()).updateNotificationBadge();
            }
            if (getView() != null) {
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        String.format("%s added to cart", product.getName()), Snackbar.LENGTH_SHORT).show();
            }
            dismiss();
        });

        binding.btnDetailBuyNow.setOnClickListener(v -> {
            addToCart();
            com.example.electronicsgadgets.utils.NotificationHelper.notifyAddToCart(requireContext(), product);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateCartBadge();
                ((MainActivity) getActivity()).updateNotificationBadge();
            }
            dismiss();
            CheckoutBottomSheet.newInstance().show(getParentFragmentManager(), "checkout_bottom_sheet");
        });
    }

    private void addToCart() {
        SessionManager sessionManager = new SessionManager(requireContext());
        List<CartItem> cart = sessionManager.getCart();

        boolean existing = false;
        for (CartItem item : cart) {
            if (item.getProductId().equals(product.getProductId())) {
                int newQty = Math.min(item.getQuantity() + quantity, product.getStock());
                item.setQuantity(newQty);
                existing = true;
                break;
            }
        }

        if (!existing) {
            CartItem newItem = new CartItem(
                    "cart_" + System.currentTimeMillis(),
                    product.getProductId(),
                    product.getName(),
                    product.getBrand(),
                    product.getPrice(),
                    product.getImageUrl(),
                    quantity,
                    product.getStock()
            );
            cart.add(newItem);
        }

        sessionManager.saveCart(cart);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
