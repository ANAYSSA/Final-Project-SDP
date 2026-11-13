package com.restaurant.strategy.concrete;

import com.restaurant.builder.product.Fulfillment;
import com.restaurant.strategy.api.DiscountStrategy;
import java.math.BigDecimal;

/**
 * 15% off a large shared order, for dine-in or takeaway.
 * Eligibility depends only on the decorated item subtotal supplied by the pricing context, never on clock time.
 * The context rounds the subtotal and the returned discount to scale 2 with HALF_UP.
 */
public final class FamilyDiscount implements DiscountStrategy {
    public static final BigDecimal MINIMUM_SUBTOTAL = new BigDecimal("6000.00");
    private static final BigDecimal RATE = new BigDecimal("0.15");
    public String code() { return "FAMILY"; }
    public BigDecimal discount(BigDecimal subtotal, Fulfillment fulfillment) {
        if (subtotal.compareTo(MINIMUM_SUBTOTAL) < 0)
            throw new IllegalArgumentException("Family discount requires a subtotal of at least " + MINIMUM_SUBTOTAL
                + " KZT; this order is " + subtotal + " KZT");
        return subtotal.multiply(RATE);
    }
}
