package com.dinecore.tenant.service;

import com.dinecore.tenant.error.ApiException;
import org.springframework.http.HttpStatus;

public final class Texts {

    private Texts() {
    }

    public static String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", message);
        }
        return value.trim();
    }
}
