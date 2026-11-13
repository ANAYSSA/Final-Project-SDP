package com.restaurant.strategy.concrete;

import com.restaurant.builder.product.Fulfillment;
import com.restaurant.strategy.api.DiscountStrategy;
import java.math.BigDecimal;

public final class StudentDiscount implements DiscountStrategy {
    public String code() { return "STUDENT"; }
    public BigDecimal discount(BigDecimal subtotal, Fulfillment fulfillment) { return subtotal.multiply(new BigDecimal("0.10")); }
}
