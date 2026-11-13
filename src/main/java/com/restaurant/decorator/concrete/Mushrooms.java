package com.restaurant.decorator.concrete;

import com.restaurant.decorator.component.ToppingDecorator;
import com.restaurant.decorator.component.ToppingType;
import com.restaurant.factory.product.Meal;

public final class Mushrooms extends ToppingDecorator {
    public Mushrooms(Meal meal) { super(meal, ToppingType.MUSHROOMS); }
}
