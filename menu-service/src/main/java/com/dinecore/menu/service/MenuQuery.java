package com.dinecore.menu.service;

import com.dinecore.menu.api.MenuView;
import com.dinecore.menu.cache.MenuCache;
import com.dinecore.menu.domain.BranchMenuOverride;
import com.dinecore.menu.domain.Category;
import com.dinecore.menu.domain.Dish;
import com.dinecore.menu.domain.OverrideKey;
import com.dinecore.menu.repo.BranchMenuOverrideRepository;
import com.dinecore.menu.repo.CategoryRepository;
import com.dinecore.menu.repo.DishRepository;
import com.dinecore.menu.repo.ModifierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MenuQuery {

    private final CategoryRepository categories;
    private final DishRepository dishes;
    private final ModifierRepository modifiers;
    private final BranchMenuOverrideRepository overrides;
    private final MenuCache cache;

    public MenuQuery(CategoryRepository categories, DishRepository dishes, ModifierRepository modifiers,
                     BranchMenuOverrideRepository overrides, MenuCache cache) {
        this.categories = categories;
        this.dishes = dishes;
        this.modifiers = modifiers;
        this.overrides = overrides;
        this.cache = cache;
    }

    @Transactional(readOnly = true)
    public MenuView read(UUID organizationId, UUID branchId) {
        return cache.get(branchId).orElseGet(() -> remember(organizationId, branchId));
    }

    private MenuView remember(UUID organizationId, UUID branchId) {
        MenuView view = build(organizationId, branchId);
        cache.put(organizationId, branchId, view);
        return view;
    }

    private MenuView build(UUID organizationId, UUID branchId) {
        List<MenuView.MenuCategory> visible = categories.findByOrganizationIdAndActiveTrueOrderBySortOrderAsc(organizationId)
                .stream()
                .map(category -> toCategory(category, branchId))
                .filter(category -> !category.dishes().isEmpty())
                .toList();
        return new MenuView(branchId, visible);
    }

    private MenuView.MenuCategory toCategory(Category category, UUID branchId) {
        List<MenuView.MenuDish> visible = dishes.findByCategoryIdAndActiveTrueOrderByNameAsc(category.getId()).stream()
                .map(dish -> toDish(dish, branchId))
                .flatMap(Optional::stream)
                .toList();
        return new MenuView.MenuCategory(category.getId(), category.getName(), category.getSortOrder(), visible);
    }

    private Optional<MenuView.MenuDish> toDish(Dish dish, UUID branchId) {
        Optional<BranchMenuOverride> override = overrides.findById(new OverrideKey(branchId, dish.getId()));
        if (override.isPresent() && !override.get().isAvailable()) {
            return Optional.empty();
        }
        return Optional.of(new MenuView.MenuDish(dish.getId(), dish.getName(), dish.getDescription(),
                price(dish, override), modifierViews(dish.getId())));
    }

    private List<MenuView.MenuModifier> modifierViews(UUID dishId) {
        return modifiers.findByDishIdOrderByNameAsc(dishId).stream()
                .map(modifier -> new MenuView.MenuModifier(modifier.getId(), modifier.getName(), modifier.getPriceDelta()))
                .toList();
    }

    private static BigDecimal price(Dish dish, Optional<BranchMenuOverride> override) {
        return override.map(BranchMenuOverride::getCustomPrice).orElse(dish.getBasePrice());
    }
}
