package com.dinecore.menu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "branch_menu_override")
public class BranchMenuOverride {

    @EmbeddedId
    private OverrideKey id;

    @Column(name = "custom_price", precision = 12, scale = 2)
    private BigDecimal customPrice;

    @Column(nullable = false)
    private boolean available;

    protected BranchMenuOverride() {
    }

    public BranchMenuOverride(OverrideKey id, BigDecimal customPrice, boolean available) {
        this.id = id;
        this.customPrice = customPrice;
        this.available = available;
    }

    public void update(BigDecimal customPrice, boolean available) {
        this.customPrice = customPrice;
        this.available = available;
    }

    public OverrideKey getId() {
        return id;
    }

    public BigDecimal getCustomPrice() {
        return customPrice;
    }

    public boolean isAvailable() {
        return available;
    }
}
