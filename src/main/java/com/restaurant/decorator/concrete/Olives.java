package com.restaurant.decorator.concrete;

import com.restaurant.decorator.component.ToppingDecorator;
import com.restaurant.decorator.component.ToppingType;
import com.restaurant.factory.product.Meal;

public final class Olives extends ToppingDecorator {
    public Olives(Meal meal) { super(meal, ToppingType.OLIVES); }
}
