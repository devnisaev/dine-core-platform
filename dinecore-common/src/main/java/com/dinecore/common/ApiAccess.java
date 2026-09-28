package com.dinecore.common;

public final class ApiAccess {

    private ApiAccess() {
    }

    public static AccessVerdict decide(String method, String path, String role, String claimTenant, String header) {
        String httpMethod = method == null ? "" : method.toUpperCase();
        String httpPath = normalize(path);
        if (isPublic(httpMethod, httpPath)) {
            return AccessVerdict.allow();
        }
        if (role == null || role.isBlank()) {
            return AccessVerdict.unauthorized();
        }
        if (!roleAllowed(httpMethod, httpPath, role)) {
            return AccessVerdict.forbidden("FORBIDDEN", "Not allowed");
        }
        return tenantVerdict(role, httpPath, claimTenant, header);
    }

    private static AccessVerdict tenantVerdict(String role, String path, String claimTenant, String header) {
        if (needsHeader(role, path) && (header == null || header.isBlank())) {
            return AccessVerdict.forbidden("TENANT_MISMATCH", "Tenant header is required");
        }
        if (mismatches(role, claimTenant, header)) {
            return AccessVerdict.forbidden("TENANT_MISMATCH", "Tenant header does not match the token");
        }
        return AccessVerdict.allow();
    }

    private static boolean roleAllowed(String method, String path, String role) {
        if (identityPath(path)) {
            return identityRole(method, path, role);
        }
        if (menuPath(path)) {
            return menuRole(method, path, role);
        }
        return known(role) && futureApi(path);
    }

    private static boolean identityPath(String path) {
        return path.startsWith("/api/v1/organizations") || path.startsWith("/api/v1/branches")
                || path.startsWith("/api/v1/settings") || path.startsWith("/api/v1/users")
                || path.startsWith("/api/v1/shifts");
    }

    private static boolean identityRole(String method, String path, String role) {
        if (path.startsWith("/api/v1/organizations")) {
            return is(role, Role.SUPER_ADMIN);
        }
        if (path.startsWith("/api/v1/branches")) {
            return branchRole(method, role);
        }
        if (path.startsWith("/api/v1/settings")) {
            return settingsRole(method, role);
        }
        if (path.startsWith("/api/v1/users")) {
            return is(role, Role.SUPER_ADMIN) || is(role, Role.BRANCH_ADMIN);
        }
        return shiftRole(role);
    }

    private static boolean menuPath(String path) {
        return path.startsWith("/api/v1/menu") || path.startsWith("/api/v1/categories")
                || path.startsWith("/api/v1/dishes");
    }

    private static boolean menuRole(String method, String path, String role) {
        if (path.startsWith("/api/v1/menu")) {
            return "GET".equals(method) && known(role);
        }
        if (path.endsWith("/override")) {
            return "PUT".equals(method) && is(role, Role.BRANCH_ADMIN);
        }
        return ("POST".equals(method) || "PATCH".equals(method)) && is(role, Role.SUPER_ADMIN);
    }

    private static boolean branchRole(String method, String role) {
        if ("POST".equals(method)) {
            return is(role, Role.SUPER_ADMIN);
        }
        return "GET".equals(method) && (is(role, Role.SUPER_ADMIN) || is(role, Role.BRANCH_ADMIN));
    }

    private static boolean settingsRole(String method, String role) {
        if ("PUT".equals(method)) {
            return is(role, Role.BRANCH_ADMIN);
        }
        return "GET".equals(method) && known(role);
    }

    private static boolean shiftRole(String role) {
        return is(role, Role.WAITER) || is(role, Role.KITCHEN) || is(role, Role.BRANCH_ADMIN);
    }

    private static boolean needsHeader(String role, String path) {
        if (path.startsWith("/api/v1/settings") || path.startsWith("/api/v1/shifts")
                || path.startsWith("/api/v1/menu") || path.endsWith("/override") || futureApi(path)) {
            return true;
        }
        return path.startsWith("/api/v1/users") && !is(role, Role.SUPER_ADMIN);
    }

    private static boolean mismatches(String role, String claimTenant, String header) {
        if (is(role, Role.SUPER_ADMIN) || header == null || header.isBlank()) {
            return false;
        }
        return !header.equals(claimTenant);
    }

    private static boolean futureApi(String path) {
        return path.startsWith("/api/v1/tables") || path.startsWith("/api/v1/orders")
                || path.startsWith("/api/v1/cheques") || path.startsWith("/api/v1/reports")
                || path.startsWith("/ws");
    }

    private static boolean isPublic(String method, String path) {
        return "OPTIONS".equals(method) || path.startsWith("/actuator/health")
                || ("POST".equals(method) && "/api/v1/auth/login".equals(path));
    }

    private static boolean known(String role) {
        return is(role, Role.SUPER_ADMIN) || is(role, Role.BRANCH_ADMIN)
                || is(role, Role.WAITER) || is(role, Role.KITCHEN);
    }

    private static boolean is(String role, Role expected) {
        return expected.name().equals(role);
    }

    private static String normalize(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String value = path.split("\\?")[0];
        if (value.length() > 1 && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
