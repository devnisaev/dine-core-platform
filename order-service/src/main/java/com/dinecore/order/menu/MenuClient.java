package com.dinecore.order.menu;

import java.util.List;
import java.util.UUID;

public interface MenuClient {

    PricedDish price(UUID branchId, UUID dishId, List<UUID> modifierIds, String authorization);
}
