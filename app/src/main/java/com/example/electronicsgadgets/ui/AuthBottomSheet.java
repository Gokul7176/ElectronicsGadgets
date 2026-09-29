package com.example.electronicsgadgets.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.electronicsgadgets.MainActivity;
import com.example.electronicsgadgets.databinding.DialogAuthBinding;
import com.example.electronicsgadgets.firebase.FirebaseHelper;
import com.example.electronicsgadgets.models.User;
import com.example.electronicsgadgets.utils.ValidationUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AuthBottomSheet extends BottomSheetDialogFragment {

    public interface OnAuthSuccessListener {
        void onAuthSuccess();
    }

    private DialogAuthBinding binding;
    private FirebaseHelper firebaseHelper;
    private boolean isLoginMode = true;
    private OnAuthSuccessListener listener;

    public static AuthBottomSheet newInstance() {
        return new AuthBottomSheet();
    }

    public void setOnAuthSuccessListener(OnAuthSuccessListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAuthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseHelper = new FirebaseHelper(requireContext());

        updateModeUI();

        binding.tvToggleAuthMode.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateModeUI();
        });

        binding.btnAuthSubmit.setOnClickListener(v -> submitForm());
    }

    private void updateModeUI() {
        if (isLoginMode) {
            binding.tvAuthTitle.setText("Account Login");
            binding.tvAuthSubtitle.setText("Sign in to your Tech Store account");
            binding.inputLayoutName.setVisibility(View.GONE);
            binding.inputLayoutPhone.setVisibility(View.GONE);
            binding.inputLayoutConfirmPassword.setVisibility(View.GONE);
            binding.btnAuthSubmit.setText("Login");
            binding.tvToggleAuthMode.setText("Don't have an account? Register");
        } else {
            binding.tvAuthTitle.setText("Create Account");
            binding.tvAuthSubtitle.setText("Register a new account to place orders");
            binding.inputLayoutName.setVisibility(View.VISIBLE);
            binding.inputLayoutPhone.setVisibility(View.VISIBLE);
            binding.inputLayoutConfirmPassword.setVisibility(View.VISIBLE);
            binding.btnAuthSubmit.setText("Register");
            binding.tvToggleAuthMode.setText("Already have an account? Login");
        }
    }

    private void submitForm() {
        String email = binding.etAuthEmail.getText() != null ? binding.etAuthEmail.getText().toString().trim() : "";
        String password = binding.etAuthPassword.getText() != null ? binding.etAuthPassword.getText().toString().trim() : "";

        if (!ValidationUtils.isValidEmail(email)) {
            binding.etAuthEmail.setError("Enter a valid email address");
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            binding.etAuthPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (isLoginMode) {
            binding.btnAuthSubmit.setEnabled(false);
            binding.btnAuthSubmit.setText("Logging in...");

            firebaseHelper.loginUser(email, password, new FirebaseHelper.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Welcome back, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onAuthSuccess();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refreshCurrentFragment();
                    }
                    dismiss();
                }

                @Override
                public void onFailure(String errorMessage) {
                    if (!isAdded()) return;
                    binding.btnAuthSubmit.setEnabled(true);
                    binding.btnAuthSubmit.setText("Login");
                    Toast.makeText(requireContext(), "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });

        } else {
            String name = binding.etAuthName.getText() != null ? binding.etAuthName.getText().toString().trim() : "";
            String phone = binding.etAuthPhone.getText() != null ? binding.etAuthPhone.getText().toString().trim() : "";
            String confirmPassword = binding.etAuthConfirmPassword.getText() != null ? binding.etAuthConfirmPassword.getText().toString().trim() : "";

            if (ValidationUtils.isEmpty(name)) {
                binding.etAuthName.setError("Name is required");
                return;
            }

            if (!ValidationUtils.isValidPhone(phone)) {
                binding.etAuthPhone.setError("Enter a valid 10-digit phone number");
                return;
            }

            if (!password.equals(confirmPassword)) {
                binding.etAuthConfirmPassword.setError("Passwords do not match");
                return;
            }

            binding.btnAuthSubmit.setEnabled(false);
            binding.btnAuthSubmit.setText("Creating Account...");

            firebaseHelper.registerUser(name, email, phone, password, new FirebaseHelper.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onAuthSuccess();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refreshCurrentFragment();
                    }
                    dismiss();
                }

                @Override
                public void onFailure(String errorMessage) {
                    if (!isAdded()) return;
                    binding.btnAuthSubmit.setEnabled(true);
                    binding.btnAuthSubmit.setText("Register");
                    Toast.makeText(requireContext(), "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
