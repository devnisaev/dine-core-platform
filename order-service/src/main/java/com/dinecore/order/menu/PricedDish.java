package com.dinecore.order.menu;

import java.math.BigDecimal;

public record PricedDish(String name, BigDecimal unitPrice, String modifiersJson) {
}
