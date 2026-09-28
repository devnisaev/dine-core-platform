package com.dinecore.order.api;

import com.dinecore.order.domain.DiningOrder;
import com.dinecore.order.domain.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, UUID tableId, UUID waiterId, String status, String note, Long version,
                            List<ItemResponse> items) {

    public static OrderResponse from(DiningOrder order, List<OrderItem> items) {
        return new OrderResponse(order.getId(), order.getTableId(), order.getWaiterId(), order.getStatus().name(),
                order.getNote(), order.getVersion(), items.stream().map(ItemResponse::from).toList());
    }

    public record ItemResponse(UUID id, UUID dishId, String name, BigDecimal unitPrice, int quantity, String status) {

        public static ItemResponse from(OrderItem item) {
            String status = item.getStatus() == null ? null : item.getStatus().name();
            return new ItemResponse(item.getId(), item.getDishId(), item.getNameSnapshot(), item.getUnitPrice(),
                    item.getQuantity(), status);
        }
    }
}
