package com.restaurant.strategy.concrete;

import com.restaurant.builder.product.Fulfillment;
import com.restaurant.strategy.api.DiscountStrategy;
import java.math.BigDecimal;

public final class NoDiscount implements DiscountStrategy {
    public String code() { return "NONE"; }
    public BigDecimal discount(BigDecimal subtotal, Fulfillment fulfillment) { return BigDecimal.ZERO; }
}
