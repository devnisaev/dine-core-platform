package com.dinecore.order.menu;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RemoteMenu(UUID branchId, List<RemoteCategory> categories) {

    public record RemoteCategory(UUID id, String name, int sortOrder, List<RemoteDish> dishes) {
    }

    public record RemoteDish(UUID id, String name, String description, BigDecimal price, List<RemoteModifier> modifiers) {
    }

    public record RemoteModifier(UUID id, String name, BigDecimal priceDelta) {
    }
}
