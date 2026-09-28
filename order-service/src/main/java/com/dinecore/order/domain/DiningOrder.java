package com.dinecore.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.UUID;

@Entity
@Table(name = "orders")
public class DiningOrder {

    @Id
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "table_id", nullable = false)
    private UUID tableId;

    @Column(name = "waiter_id", nullable = false)
    private UUID waiterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(length = 500)
    private String note;

    @Version
    private Long version;

    protected DiningOrder() {
    }

    public DiningOrder(UUID id, UUID branchId, UUID tableId, UUID waiterId, String note) {
        this.id = id;
        this.branchId = branchId;
        this.tableId = tableId;
        this.waiterId = waiterId;
        this.status = OrderStatus.DRAFT;
        this.note = note;
    }

    public void sentToKitchen() {
        this.status = OrderStatus.SENT_TO_KITCHEN;
    }

    public void ready() {
        this.status = OrderStatus.READY;
    }

    public void served() {
        this.status = OrderStatus.SERVED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public UUID getTableId() {
        return tableId;
    }

    public UUID getWaiterId() {
        return waiterId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public Long getVersion() {
        return version;
    }
}
