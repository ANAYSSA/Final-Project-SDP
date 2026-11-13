package com.restaurant.factory.creator;

import com.restaurant.factory.product.*;

public final class PastaFactory extends MealFactory {
    public PastaFactory() { super(MealCategory.PASTA); }
    @Override protected Meal createMeal(MenuDefinition definition) { return new Pasta(definition); }
}
