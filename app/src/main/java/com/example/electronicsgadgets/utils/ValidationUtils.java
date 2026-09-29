package com.example.electronicsgadgets.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {

    public static boolean isEmpty(String text) {
        return text == null || TextUtils.isEmpty(text.trim());
    }

    public static boolean isValidEmail(String email) {
        return !isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return !isEmpty(password) && password.trim().length() >= 6;
    }

    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) return false;
        return password.equals(confirmPassword);
    }

    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        String digits = phone.trim().replaceAll("[^0-9]", "");
        return digits.length() >= 10 && digits.length() <= 13;
    }

    public static boolean isValidPinCode(String pinCode) {
        if (isEmpty(pinCode)) return false;
        String digits = pinCode.trim();
        return digits.matches("\\d{6}");
    }
}
