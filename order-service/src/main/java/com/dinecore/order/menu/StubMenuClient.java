package com.dinecore.order.menu;

import com.dinecore.order.error.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "dinecore.orders.menu", havingValue = "stub")
public class StubMenuClient implements MenuClient {

    private final Map<UUID, PricedDish> dishes = new ConcurrentHashMap<>();

    public void set(UUID dishId, String name, BigDecimal price) {
        dishes.put(dishId, new PricedDish(name, price, "[]"));
    }

    @Override
    public PricedDish price(UUID branchId, UUID dishId, List<UUID> modifierIds, String authorization) {
        PricedDish dish = dishes.get(dishId);
        if (dish == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ORDER_STATE", "Dish is not on the menu");
        }
        return dish;
    }
}
