package com.dinecore.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "dish_id", nullable = false)
    private UUID dishId;

    @Column(name = "name_snapshot", length = 160)
    private String nameSnapshot;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "modifiers_json", length = 2000)
    private String modifiersJson;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ItemStatus status;

    protected OrderItem() {
    }

    public OrderItem(UUID id, UUID orderId, UUID dishId, int quantity, String modifiersJson) {
        this.id = id;
        this.orderId = orderId;
        this.dishId = dishId;
        this.quantity = quantity;
        this.modifiersJson = modifiersJson;
    }

    public void snapshot(String name, BigDecimal unitPrice, String modifiersJson) {
        this.nameSnapshot = name;
        this.unitPrice = unitPrice;
        this.modifiersJson = modifiersJson;
        this.status = ItemStatus.QUEUED;
    }

    public void markReady() {
        this.status = ItemStatus.READY;
    }

    public void cancel() {
        this.status = ItemStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getDishId() {
        return dishId;
    }

    public String getNameSnapshot() {
        return nameSnapshot;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getModifiersJson() {
        return modifiersJson;
    }

    public ItemStatus getStatus() {
        return status;
    }
}
