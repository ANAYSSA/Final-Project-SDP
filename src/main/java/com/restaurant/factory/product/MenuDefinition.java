package com.restaurant.factory.product;

import java.math.BigDecimal;

public record MenuDefinition(String id, String name, String description, MealCategory category, BigDecimal price) { }
