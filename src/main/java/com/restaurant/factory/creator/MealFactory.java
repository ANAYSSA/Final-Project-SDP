package com.restaurant.factory.creator;

import com.restaurant.factory.product.Meal;
import com.restaurant.factory.product.MealCategory;
import com.restaurant.factory.product.MenuDefinition;
import java.util.Objects;

/** Creator: the public operation delegates product construction to a factory method. */
public abstract class MealFactory {
    private final MealCategory category;
    protected MealFactory(MealCategory category) { this.category = category; }
    public final Meal create(MenuDefinition definition) {
        Objects.requireNonNull(definition, "A menu definition is required");
        if (definition.category() != category) throw new IllegalArgumentException("Wrong meal category for factory");
        return createMeal(definition);
    }
    protected abstract Meal createMeal(MenuDefinition definition);
}
