package com.dinecore.common;

public record ApiError(String code, String message, String traceId) {

    public String json() {
        return "{\"code\":\"" + escape(code) + "\",\"message\":\"" + escape(message) + "\",\"traceId\":\"" + escape(traceId) + "\"}";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
