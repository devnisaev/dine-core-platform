package com.dinecore.menu.api;

import com.dinecore.menu.domain.Dish;

import java.math.BigDecimal;
import java.util.UUID;

public record DishResponse(UUID id, UUID categoryId, String name, String description, BigDecimal basePrice, boolean active) {

    public static DishResponse from(Dish dish) {
        return new DishResponse(dish.getId(), dish.getCategoryId(), dish.getName(), dish.getDescription(),
                dish.getBasePrice(), dish.isActive());
    }
}
