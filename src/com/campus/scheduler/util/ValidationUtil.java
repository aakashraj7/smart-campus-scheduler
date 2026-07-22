package com.campus.scheduler.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // Standard email pattern: user@domain.extension
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    // Standard Indian 10-digit mobile number pattern starting with 6-9
    private static final Pattern MOBILE_PATTERN = 
        Pattern.compile("^[6-9]\\d{9}$");

    // Code / ID pattern: alphanumeric with hyphen/underscore, 2 to 20 chars (e.g. FAC001, CS301, CSE-3A)
    private static final Pattern CODE_PATTERN = 
        Pattern.compile("^[A-Za-z0-9_-]{2,20}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidMobile(String mobile) {
        return mobile != null && MOBILE_PATTERN.matcher(mobile.trim()).matches();
    }

    public static boolean isValidCode(String code) {
        return code != null && CODE_PATTERN.matcher(code.trim()).matches();
    }

    public static boolean isNonEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidMinLength(String text, int minLength) {
        return text != null && text.trim().length() >= minLength;
    }

    public static boolean isValidIntRange(String str, int min, int max) {
        if (str == null) return false;
        try {
            int val = Integer.parseInt(str.trim());
            return val >= min && val <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int parseIntOrDefault(String str, int defaultVal) {
        try {
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
