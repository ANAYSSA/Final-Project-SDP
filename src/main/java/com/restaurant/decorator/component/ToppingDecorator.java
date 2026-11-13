package com.restaurant.decorator.component;

import com.restaurant.factory.product.Meal;
import com.restaurant.factory.product.MealCategory;
import java.math.BigDecimal;
import java.util.Objects;

/** Decorators preserve the Meal contract and wrap another Meal, including a decorator. */
public abstract class ToppingDecorator implements Meal {
    protected final Meal wrapped;
    private final String toppingName;
    private final BigDecimal extraPrice;
    protected ToppingDecorator(Meal wrapped, ToppingType topping) {
        this.wrapped = Objects.requireNonNull(wrapped, "A meal is required");
        if (wrapped.category() == MealCategory.DRINK) throw new IllegalArgumentException("Toppings are available for pizza and pasta only");
        this.toppingName = topping.displayName();
        this.extraPrice = topping.price();
    }
    public final String id() { return wrapped.id(); }
    public final String name() { return wrapped.name() + " + " + toppingName; }
    public final String description() { return wrapped.description() + ", " + toppingName.toLowerCase(); }
    public final MealCategory category() { return wrapped.category(); }
    public final BigDecimal price() { return wrapped.price().add(extraPrice); }
}
