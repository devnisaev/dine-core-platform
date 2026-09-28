package com.dinecore.menu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class OverrideKey implements Serializable {

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "dish_id", nullable = false)
    private UUID dishId;

    protected OverrideKey() {
    }

    public OverrideKey(UUID branchId, UUID dishId) {
        this.branchId = branchId;
        this.dishId = dishId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public UUID getDishId() {
        return dishId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OverrideKey key)) {
            return false;
        }
        return Objects.equals(branchId, key.branchId) && Objects.equals(dishId, key.dishId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId, dishId);
    }
}
