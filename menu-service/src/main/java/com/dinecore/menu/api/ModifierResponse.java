package com.dinecore.menu.api;

import com.dinecore.menu.domain.Modifier;

import java.math.BigDecimal;
import java.util.UUID;

public record ModifierResponse(UUID id, String name, BigDecimal priceDelta) {

    public static ModifierResponse from(Modifier modifier) {
        return new ModifierResponse(modifier.getId(), modifier.getName(), modifier.getPriceDelta());
    }
}
