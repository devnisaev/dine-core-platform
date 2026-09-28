package com.dinecore.order.event;

import com.dinecore.order.domain.DiningOrder;
import com.dinecore.order.domain.OrderItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

public final class OrderPayloads {

    private OrderPayloads() {
    }

    public record SubmittedItem(UUID dishId, String name, int qty, BigDecimal unitPrice, String modifiers) {
    }

    public record Submitted(UUID orderId, int tableNumber, UUID waiterId, List<SubmittedItem> items) {
    }

    public record Ready(UUID orderId, UUID waiterId) {
    }

    public record ServedLine(String name, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    public record Served(UUID orderId, UUID tableId, UUID waiterId, List<ServedLine> lines) {
    }

    public record Cancelled(UUID orderId, String reason) {
    }

    public static Submitted submitted(DiningOrder order, int tableNumber, List<OrderItem> items) {
        List<SubmittedItem> lines = items.stream()
                .map(item -> new SubmittedItem(item.getDishId(), item.getNameSnapshot(), item.getQuantity(),
                        item.getUnitPrice(), item.getModifiersJson()))
                .toList();
        return new Submitted(order.getId(), tableNumber, order.getWaiterId(), lines);
    }

    public static Served served(DiningOrder order, List<OrderItem> items) {
        return new Served(order.getId(), order.getTableId(), order.getWaiterId(), items.stream().map(OrderPayloads::line).toList());
    }

    private static ServedLine line(OrderItem item) {
        BigDecimal total = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
        return new ServedLine(item.getNameSnapshot(), item.getQuantity(), item.getUnitPrice(), total);
    }
}
