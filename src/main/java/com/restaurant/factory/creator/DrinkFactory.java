package com.restaurant.factory.creator;

import com.restaurant.factory.product.*;

public final class DrinkFactory extends MealFactory {
    public DrinkFactory() { super(MealCategory.DRINK); }
    @Override protected Meal createMeal(MenuDefinition definition) { return new Drink(definition); }
}
