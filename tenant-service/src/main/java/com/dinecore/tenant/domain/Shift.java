package com.dinecore.tenant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shift")
public class Shift {

    @Id
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    protected Shift() {
    }

    public Shift(UUID id, UUID branchId, UUID userId, Instant openedAt, Instant closedAt) {
        this.id = id;
        this.branchId = branchId;
        this.userId = userId;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
    }

    public void close(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }
}
