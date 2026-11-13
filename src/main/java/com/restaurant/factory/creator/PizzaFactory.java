package com.restaurant.factory.creator;

import com.restaurant.factory.product.*;

public final class PizzaFactory extends MealFactory {
    public PizzaFactory() { super(MealCategory.PIZZA); }
    @Override protected Meal createMeal(MenuDefinition definition) { return new Pizza(definition); }
}
