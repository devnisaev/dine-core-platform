package com.dinecore.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "dining_table")
public class DiningTable {

    @Id
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "table_number", nullable = false)
    private int tableNumber;

    @Column(nullable = false)
    private int seats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TableStatus status;

    protected DiningTable() {
    }

    public DiningTable(UUID id, UUID branchId, int tableNumber, int seats) {
        this.id = id;
        this.branchId = branchId;
        this.tableNumber = tableNumber;
        this.seats = seats;
        this.status = TableStatus.AVAILABLE;
    }

    public void occupy() {
        this.status = TableStatus.OCCUPIED;
    }

    public void available() {
        this.status = TableStatus.AVAILABLE;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getSeats() {
        return seats;
    }

    public TableStatus getStatus() {
        return status;
    }
}
