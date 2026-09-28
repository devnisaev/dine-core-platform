package com.dinecore.common;

public record AccessVerdict(Decision decision, String code, String message) {

    public enum Decision {
        ALLOW,
        UNAUTHORIZED,
        FORBIDDEN
    }

    public static AccessVerdict allow() {
        return new AccessVerdict(Decision.ALLOW, "", "");
    }

    public static AccessVerdict unauthorized() {
        return new AccessVerdict(Decision.UNAUTHORIZED, "AUTH", "Authentication required");
    }

    public static AccessVerdict forbidden(String code, String message) {
        return new AccessVerdict(Decision.FORBIDDEN, code, message);
    }

    public boolean allowed() {
        return decision == Decision.ALLOW;
    }
}
