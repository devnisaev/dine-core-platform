package com.dinecore.menu.service;

import com.dinecore.menu.api.OverrideResponse;
import com.dinecore.menu.domain.BranchMenuOverride;
import com.dinecore.menu.domain.Dish;
import com.dinecore.menu.domain.OverrideKey;
import com.dinecore.menu.error.ApiException;
import com.dinecore.menu.event.MenuNotifier;
import com.dinecore.menu.repo.BranchMenuOverrideRepository;
import com.dinecore.menu.repo.DishRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OverrideService {

    private final DishRepository dishes;
    private final BranchMenuOverrideRepository overrides;
    private final MenuNotifier notifier;

    public OverrideService(DishRepository dishes, BranchMenuOverrideRepository overrides, MenuNotifier notifier) {
        this.dishes = dishes;
        this.overrides = overrides;
        this.notifier = notifier;
    }

    @Transactional
    public OverrideResponse put(UUID organizationId, UUID branchId, UUID dishId, BigDecimal customPrice, Boolean available) {
        requireAvailable(available);
        requireDish(organizationId, dishId);
        BranchMenuOverride override = overrides.findById(new OverrideKey(branchId, dishId))
                .orElseGet(() -> new BranchMenuOverride(new OverrideKey(branchId, dishId), null, true));
        override.update(Checks.optionalMoney(customPrice, "Custom price"), available);
        overrides.save(override);
        notifier.branchChanged(organizationId, branchId);
        return OverrideResponse.from(override);
    }

    private void requireDish(UUID organizationId, UUID dishId) {
        Dish dish = dishes.findById(dishId).orElseThrow(() -> missing());
        if (!organizationId.equals(dish.getOrganizationId())) {
            throw missing();
        }
    }

    private static void requireAvailable(Boolean available) {
        if (available == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Available flag is required");
        }
    }

    private static ApiException missing() {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Dish not found");
    }
}
