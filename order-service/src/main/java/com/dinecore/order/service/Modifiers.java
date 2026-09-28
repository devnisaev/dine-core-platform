package com.dinecore.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Modifiers {

    private Modifiers() {
    }

    public static String write(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return "[]";
        }
        return ids.stream().map(id -> "\"" + id + "\"").reduce((left, right) -> left + "," + right)
                .map(body -> "[" + body + "]")
                .orElse("[]");
    }

    public static List<UUID> readIds(String raw) {
        if (raw == null || raw.isBlank() || "[]".equals(raw.trim())) {
            return List.of();
        }
        String body = raw.trim();
        body = body.substring(1, body.length() - 1);
        List<UUID> ids = new ArrayList<>();
        for (String part : body.split(",")) {
            ids.add(UUID.fromString(part.trim().replace("\"", "")));
        }
        return ids;
    }
}
