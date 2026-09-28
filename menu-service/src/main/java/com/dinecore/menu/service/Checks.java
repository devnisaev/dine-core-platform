package com.dinecore.menu.service;

import com.dinecore.menu.error.ApiException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Checks {

    private Checks() {
    }

    public static String required(String value, String name, int max) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw invalid(name + " is required");
        }
        return bounded(text, name, max);
    }

    public static String optional(String value, String name, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return bounded(value.trim(), name, max);
    }

    public static BigDecimal money(BigDecimal value, String name) {
        if (value == null || value.signum() < 0) {
            throw invalid(name + " must be zero or positive");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal optionalMoney(BigDecimal value, String name) {
        if (value == null) {
            return null;
        }
        return money(value, name);
    }

    private static String bounded(String text, String name, int max) {
        if (text.length() > max) {
            throw invalid(name + " is too long");
        }
        return text;
    }

    private static ApiException invalid(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", message);
    }
}
