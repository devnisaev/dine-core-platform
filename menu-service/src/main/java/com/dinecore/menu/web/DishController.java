package com.dinecore.menu.web;

import com.dinecore.menu.api.DishResponse;
import com.dinecore.menu.api.ModifierResponse;
import com.dinecore.menu.api.OverrideResponse;
import com.dinecore.menu.security.CurrentUser;
import com.dinecore.menu.service.CatalogService;
import com.dinecore.menu.service.OverrideService;
import com.dinecore.menu.service.TenantIds;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dishes")
public class DishController {

    private final CatalogService catalog;
    private final OverrideService overrides;

    public DishController(CatalogService catalog, OverrideService overrides) {
        this.catalog = catalog;
        this.overrides = overrides;
    }

    @PostMapping
    public DishResponse create(@RequestBody CreateDishRequest request) {
        return catalog.createDish(CurrentUser.organizationId(), request.categoryId(), request.name(),
                request.description(), request.basePrice());
    }

    @PatchMapping("/{id}")
    public DishResponse patch(@PathVariable UUID id, @RequestBody PatchDishRequest request) {
        return catalog.patchDish(CurrentUser.organizationId(), id, request.categoryId(), request.name(),
                request.description(), request.basePrice(), request.active());
    }

    @PostMapping("/{id}/modifiers")
    public ModifierResponse createModifier(@PathVariable UUID id, @RequestBody ModifierRequest request) {
        return catalog.createModifier(CurrentUser.organizationId(), id, request.name(), request.priceDelta());
    }

    @PatchMapping("/{id}/modifiers/{modifierId}")
    public ModifierResponse patchModifier(@PathVariable UUID id, @PathVariable UUID modifierId, @RequestBody ModifierRequest request) {
        return catalog.patchModifier(CurrentUser.organizationId(), id, modifierId, request.name(), request.priceDelta());
    }

    @PutMapping("/{id}/override")
    public OverrideResponse override(@PathVariable UUID id, @RequestBody OverrideRequest request) {
        return overrides.put(CurrentUser.organizationId(), TenantIds.current(), id, request.customPrice(), request.available());
    }

    public record CreateDishRequest(UUID categoryId, String name, String description, BigDecimal basePrice) {
    }

    public record PatchDishRequest(UUID categoryId, String name, String description, BigDecimal basePrice, Boolean active) {
    }

    public record ModifierRequest(String name, BigDecimal priceDelta) {
    }

    public record OverrideRequest(BigDecimal customPrice, Boolean available) {
    }
}
