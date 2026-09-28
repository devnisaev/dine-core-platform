package com.dinecore.menu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "modifier")
public class Modifier {

    @Id
    private UUID id;

    @Column(name = "dish_id", nullable = false)
    private UUID dishId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "price_delta", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceDelta;

    protected Modifier() {
    }

    public Modifier(UUID id, UUID dishId, String name, BigDecimal priceDelta) {
        this.id = id;
        this.dishId = dishId;
        this.name = name;
        this.priceDelta = priceDelta;
    }

    public void apply(String name, BigDecimal priceDelta) {
        if (name != null) {
            this.name = name;
        }
        if (priceDelta != null) {
            this.priceDelta = priceDelta;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getDishId() {
        return dishId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPriceDelta() {
        return priceDelta;
    }
}
