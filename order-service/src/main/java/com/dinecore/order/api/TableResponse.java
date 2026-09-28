package com.dinecore.order.api;

import com.dinecore.order.domain.DiningTable;

import java.util.UUID;

public record TableResponse(UUID id, UUID branchId, int tableNumber, int seats, String status) {

    public static TableResponse from(DiningTable table) {
        return new TableResponse(table.getId(), table.getBranchId(), table.getTableNumber(), table.getSeats(),
                table.getStatus().name());
    }
}
