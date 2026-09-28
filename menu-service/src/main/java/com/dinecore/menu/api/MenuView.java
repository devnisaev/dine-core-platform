package com.dinecore.menu.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MenuView(UUID branchId, List<MenuCategory> categories) {

    public record MenuCategory(UUID id, String name, int sortOrder, List<MenuDish> dishes) {
    }

    public record MenuDish(UUID id, String name, String description, BigDecimal price, List<MenuModifier> modifiers) {
    }

    public record MenuModifier(UUID id, String name, BigDecimal priceDelta) {
    }
}
