package com.dinecore.menu.api;

import com.dinecore.menu.domain.Category;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, int sortOrder, boolean active) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSortOrder(), category.isActive());
    }
}
