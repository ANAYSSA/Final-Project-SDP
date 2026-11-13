package com.restaurant.factory.product;

import java.math.BigDecimal;
import java.util.Objects;

/** Shared immutable product state; each concrete product has a distinct type. */
public abstract class BaseMeal implements Meal {
    private final MenuDefinition definition;
    protected BaseMeal(MenuDefinition definition) { this.definition = Objects.requireNonNull(definition); }
    public final String id() { return definition.id(); }
    public final String name() { return definition.name(); }
    public final String description() { return definition.description(); }
    public final MealCategory category() { return definition.category(); }
    public final BigDecimal price() { return definition.price(); }
}
