package com.restaurant.strategy.concrete;

import com.restaurant.builder.product.Fulfillment;
import com.restaurant.strategy.api.DiscountStrategy;
import java.math.BigDecimal;

public final class PickupDiscount implements DiscountStrategy {
    public String code() { return "PICKUP"; }
    public BigDecimal discount(BigDecimal subtotal, Fulfillment fulfillment) {
        if (fulfillment != Fulfillment.TAKEAWAY) throw new IllegalArgumentException("Pickup discount requires takeaway");
        return subtotal.multiply(new BigDecimal("0.20"));
    }
}
