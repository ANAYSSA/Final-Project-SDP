package com.restaurant.strategy.context;

import com.restaurant.builder.product.Fulfillment;
import com.restaurant.builder.product.OrderLine;
import com.restaurant.strategy.api.DiscountStrategy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/** Context accepts an interchangeable strategy; domain money is never represented by double. */
public final class PricingCalculator {
    public Pricing calculate(List<OrderLine> lines, Fulfillment fulfillment, DiscountStrategy strategy) {
        Objects.requireNonNull(fulfillment, "Choose dine-in or takeaway");
        Objects.requireNonNull(strategy, "A discount strategy is required");
        if (lines == null || lines.isEmpty() || lines.size() > 30) throw new IllegalArgumentException("An order must have 1 to 30 lines");
        BigDecimal subtotal = lines.stream().map(OrderLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = strategy.discount(subtotal, fulfillment).setScale(2, RoundingMode.HALF_UP);
        if (discount.signum() < 0 || discount.compareTo(subtotal) > 0) throw new IllegalArgumentException("Invalid discount amount");
        return new Pricing(subtotal, discount, subtotal.subtract(discount), "KZT");
    }
}
