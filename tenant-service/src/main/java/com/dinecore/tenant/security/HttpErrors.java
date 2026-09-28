package com.dinecore.tenant.security;

import com.dinecore.common.ApiError;
import com.dinecore.common.RequestHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

public final class HttpErrors {

    private HttpErrors() {
    }

    public static void write(HttpServletResponse response, int status, String code, String message, HttpServletRequest request)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(new ApiError(code, message, traceId(request)).json());
    }

    private static String traceId(HttpServletRequest request) {
        String traceId = request.getHeader(RequestHeaders.REQUEST_ID);
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return traceId;
    }
}
