package com.example.electronicsgadgets.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.electronicsgadgets.MainActivity;
import com.example.electronicsgadgets.databinding.FragmentProfileBinding;
import com.example.electronicsgadgets.models.User;
import com.example.electronicsgadgets.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        setupClickListeners();
        updateUI();
    }

    private void setupClickListeners() {
        binding.btnLoginRegister.setOnClickListener(v -> {
            AuthBottomSheet authBottomSheet = AuthBottomSheet.newInstance();
            authBottomSheet.setOnAuthSuccessListener(this::updateUI);
            authBottomSheet.show(getParentFragmentManager(), "auth_bottom_sheet");
        });

        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            updateUI();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateCartBadge();
            }
        });

        binding.cardMyOrders.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToOrders();
            }
        });

        binding.cardAdminManageProducts.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AdminProductActivity.class);
            startActivity(intent);
        });

        binding.cardAdminAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AdminAddEditProductActivity.class);
            startActivity(intent);
        });

        binding.cardAdminViewAllOrders.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToOrders();
            }
        });
    }

    public void updateUI() {
        if (!isAdded() || binding == null) return;

        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getUserDetails();
            binding.layoutGuestActions.setVisibility(View.GONE);
            binding.layoutLoggedInActions.setVisibility(View.VISIBLE);

            if (user != null) {
                binding.tvProfileName.setText(user.getName());
                binding.tvProfileEmail.setText(user.getEmail());
                binding.tvProfilePhone.setText(user.getPhone());
                binding.tvProfilePhone.setVisibility(View.VISIBLE);

                if ("admin".equalsIgnoreCase(user.getRole())) {
                    binding.tvProfileRoleBadge.setText("System Admin");
                    binding.tvProfileRoleBadge.setVisibility(View.VISIBLE);
                    binding.layoutAdminSection.setVisibility(View.VISIBLE);
                    binding.cardMyOrders.setVisibility(View.GONE);
                    binding.tvAccountSettingsHeader.setVisibility(View.GONE);
                } else {
                    binding.tvProfileRoleBadge.setText("Customer");
                    binding.tvProfileRoleBadge.setVisibility(View.VISIBLE);
                    binding.layoutAdminSection.setVisibility(View.GONE);
                    binding.cardMyOrders.setVisibility(View.VISIBLE);
                    binding.tvAccountSettingsHeader.setVisibility(View.VISIBLE);
                }
            }
        } else {
            binding.layoutGuestActions.setVisibility(View.VISIBLE);
            binding.layoutLoggedInActions.setVisibility(View.GONE);
            binding.tvProfileName.setText("Guest User");
            binding.tvProfileEmail.setText("Please login to access your account");
            binding.tvProfilePhone.setVisibility(View.GONE);
            binding.tvProfileRoleBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
