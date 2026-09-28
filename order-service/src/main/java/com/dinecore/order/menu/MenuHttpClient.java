package com.dinecore.order.menu;

import com.dinecore.common.RequestHeaders;
import com.dinecore.order.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MenuHttpClient implements MenuClient {

    private final RestClient http;
    private final JsonMapper json;

    public MenuHttpClient(RestClient http, JsonMapper json) {
        this.http = http;
        this.json = json;
    }

    @Override
    public PricedDish price(UUID branchId, UUID dishId, List<UUID> modifierIds, String authorization) {
        RemoteMenu menu = http.get()
                .uri("/api/v1/menu")
                .header("Authorization", authorization == null ? "" : authorization)
                .header(RequestHeaders.TENANT_ID, branchId.toString())
                .retrieve()
                .body(RemoteMenu.class);
        return priced(find(menu, dishId), modifierIds);
    }

    private PricedDish priced(RemoteMenu.RemoteDish dish, List<UUID> modifierIds) {
        List<RemoteMenu.RemoteModifier> chosen = choose(dish, modifierIds);
        BigDecimal unit = dish.price().add(sum(chosen)).setScale(2, RoundingMode.HALF_UP);
        return new PricedDish(dish.name(), unit, json.writeValueAsString(chosen));
    }

    private static RemoteMenu.RemoteDish find(RemoteMenu menu, UUID dishId) {
        if (menu == null || menu.categories() == null) {
            throw missing();
        }
        return menu.categories().stream()
                .filter(category -> category.dishes() != null)
                .flatMap(category -> category.dishes().stream())
                .filter(dish -> dishId.equals(dish.id()))
                .findFirst()
                .orElseThrow(MenuHttpClient::missing);
    }

    private static List<RemoteMenu.RemoteModifier> choose(RemoteMenu.RemoteDish dish, List<UUID> modifierIds) {
        List<RemoteMenu.RemoteModifier> chosen = new ArrayList<>();
        for (UUID modifierId : modifierIds == null ? List.<UUID>of() : modifierIds) {
            chosen.add(modifier(dish, modifierId));
        }
        return chosen;
    }

    private static RemoteMenu.RemoteModifier modifier(RemoteMenu.RemoteDish dish, UUID modifierId) {
        if (dish.modifiers() == null) {
            throw invalid("Modifier is not on the dish");
        }
        return dish.modifiers().stream()
                .filter(modifier -> modifierId.equals(modifier.id()))
                .findFirst()
                .orElseThrow(() -> invalid("Modifier is not on the dish"));
    }

    private static BigDecimal sum(List<RemoteMenu.RemoteModifier> chosen) {
        return chosen.stream().map(RemoteMenu.RemoteModifier::priceDelta).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static ApiException missing() {
        return new ApiException(HttpStatus.BAD_REQUEST, "ORDER_STATE", "Dish is not on the menu");
    }

    private static ApiException invalid(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", message);
    }
}
