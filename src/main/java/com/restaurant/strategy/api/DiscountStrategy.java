package com.restaurant.strategy.api;

import com.restaurant.builder.product.Fulfillment;
import java.math.BigDecimal;

public interface DiscountStrategy {
    String code();
    BigDecimal discount(BigDecimal subtotal, Fulfillment fulfillment);
}
