package com.dinecore.order.notify;

import java.util.UUID;

public final class Destinations {

    private Destinations() {
    }

    public static String kitchen(UUID branchId) {
        return "/topic/branch/" + branchId + "/kitchen";
    }

    public static String tables(UUID branchId) {
        return "/topic/branch/" + branchId + "/tables";
    }

    public static String waiter(UUID branchId, UUID waiterId) {
        return "/topic/branch/" + branchId + "/waiter/" + waiterId;
    }
}
