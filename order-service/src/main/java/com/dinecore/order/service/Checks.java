package com.dinecore.order.service;

import com.dinecore.order.error.ApiException;
import org.springframework.http.HttpStatus;

public final class Checks {

    private Checks() {
    }

    public static int positive(int value, String name) {
        if (value < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", name + " must be at least 1");
        }
        return value;
    }

    public static String note(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        if (text.length() > 500) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Note is too long");
        }
        return text;
    }

    public static String reason(String value) {
        if (value == null || value.isBlank()) {
            return "Cancelled";
        }
        String text = value.trim();
        if (text.length() > 200) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Reason is too long");
        }
        return text;
    }
}
