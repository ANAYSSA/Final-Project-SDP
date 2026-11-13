package com.restaurant.factory.product;

import java.math.BigDecimal;

/** Common product consumed by decorators and order lines. */
public interface Meal {
    String id();
    String name();
    String description();
    MealCategory category();
    BigDecimal price();
}
