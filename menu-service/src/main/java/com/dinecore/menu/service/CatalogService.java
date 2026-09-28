package com.dinecore.menu.service;

import com.dinecore.menu.api.CategoryResponse;
import com.dinecore.menu.api.DishResponse;
import com.dinecore.menu.api.ModifierResponse;
import com.dinecore.menu.domain.Category;
import com.dinecore.menu.domain.Dish;
import com.dinecore.menu.domain.Modifier;
import com.dinecore.menu.error.ApiException;
import com.dinecore.menu.event.MenuNotifier;
import com.dinecore.menu.repo.CategoryRepository;
import com.dinecore.menu.repo.DishRepository;
import com.dinecore.menu.repo.ModifierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CatalogService {

    private final CategoryRepository categories;
    private final DishRepository dishes;
    private final ModifierRepository modifiers;
    private final MenuNotifier notifier;

    public CatalogService(CategoryRepository categories, DishRepository dishes, ModifierRepository modifiers, MenuNotifier notifier) {
        this.categories = categories;
        this.dishes = dishes;
        this.modifiers = modifiers;
        this.notifier = notifier;
    }

    @Transactional
    public CategoryResponse createCategory(UUID organizationId, String name, Integer sortOrder) {
        Category category = new Category(UUID.randomUUID(), organizationId, Checks.required(name, "Name", 120), order(sortOrder));
        categories.save(category);
        notifier.catalogChanged(organizationId);
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse patchCategory(UUID organizationId, UUID id, String name, Integer sortOrder, Boolean active) {
        Category category = category(organizationId, id);
        category.apply(name == null ? null : Checks.required(name, "Name", 120), sortOrder, active);
        notifier.catalogChanged(organizationId);
        return CategoryResponse.from(category);
    }

    @Transactional
    public DishResponse createDish(UUID organizationId, UUID categoryId, String name, String description, BigDecimal basePrice) {
        category(organizationId, categoryId);
        Dish dish = new Dish(UUID.randomUUID(), organizationId, categoryId, Checks.required(name, "Name", 160),
                Checks.optional(description, "Description", 500), Checks.money(basePrice, "Base price"));
        dishes.save(dish);
        notifier.catalogChanged(organizationId);
        return DishResponse.from(dish);
    }

    @Transactional
    public DishResponse patchDish(UUID organizationId, UUID id, UUID categoryId, String name, String description,
                                   BigDecimal basePrice, Boolean active) {
        Dish dish = dish(organizationId, id);
        if (categoryId != null) {
            category(organizationId, categoryId);
        }
        dish.apply(cleanName(name), cleanDescription(description), cleanPrice(basePrice), categoryId, active);
        notifier.catalogChanged(organizationId);
        return DishResponse.from(dish);
    }

    @Transactional
    public ModifierResponse createModifier(UUID organizationId, UUID dishId, String name, BigDecimal priceDelta) {
        dish(organizationId, dishId);
        Modifier modifier = new Modifier(UUID.randomUUID(), dishId, Checks.required(name, "Name", 120),
                Checks.money(priceDelta, "Price delta"));
        modifiers.save(modifier);
        notifier.catalogChanged(organizationId);
        return ModifierResponse.from(modifier);
    }

    @Transactional
    public ModifierResponse patchModifier(UUID organizationId, UUID dishId, UUID modifierId, String name, BigDecimal priceDelta) {
        Modifier modifier = modifier(organizationId, dishId, modifierId);
        modifier.apply(name == null ? null : Checks.required(name, "Name", 120), cleanDelta(priceDelta));
        notifier.catalogChanged(organizationId);
        return ModifierResponse.from(modifier);
    }

    private Category category(UUID organizationId, UUID id) {
        Category found = categories.findById(id).orElseThrow(() -> missing("Category not found"));
        if (!organizationId.equals(found.getOrganizationId())) {
            throw missing("Category not found");
        }
        return found;
    }

    private Dish dish(UUID organizationId, UUID id) {
        Dish found = dishes.findById(id).orElseThrow(() -> missing("Dish not found"));
        if (!organizationId.equals(found.getOrganizationId())) {
            throw missing("Dish not found");
        }
        return found;
    }

    private Modifier modifier(UUID organizationId, UUID dishId, UUID modifierId) {
        dish(organizationId, dishId);
        Modifier found = modifiers.findById(modifierId).orElseThrow(() -> missing("Modifier not found"));
        if (!dishId.equals(found.getDishId())) {
            throw missing("Modifier not found");
        }
        return found;
    }

    private static String cleanName(String name) {
        return name == null ? null : Checks.required(name, "Name", 160);
    }

    private static String cleanDescription(String description) {
        return description == null ? null : Checks.optional(description, "Description", 500);
    }

    private static BigDecimal cleanPrice(BigDecimal basePrice) {
        return basePrice == null ? null : Checks.money(basePrice, "Base price");
    }

    private static BigDecimal cleanDelta(BigDecimal priceDelta) {
        return priceDelta == null ? null : Checks.money(priceDelta, "Price delta");
    }

    private static int order(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }

    private static ApiException missing(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }
}
