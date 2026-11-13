package com.restaurant.decorator.catalog;

import com.restaurant.decorator.concrete.*;
import com.restaurant.factory.product.Meal;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class ToppingCatalog {
    public record Topping(String id, String name, BigDecimal price) { }
    private final List<Topping> toppings = java.util.Arrays.stream(com.restaurant.decorator.component.ToppingType.values())
        .map(type -> new Topping(type.id(), type.displayName(), type.price())).toList();
    private final Map<String, UnaryOperator<Meal>> decorators = Map.of(
        "cheese", ExtraCheese::new, "mushrooms", Mushrooms::new, "olives", Olives::new,
        "herbs", HerbsDecorator::new);
    public List<Topping> items() { return toppings; }
    public Meal add(Meal meal, String id) {
        UnaryOperator<Meal> decorator = decorators.get(id);
        if (decorator == null) throw new IllegalArgumentException("Unknown topping: " + id);
        return decorator.apply(meal);
    }
    public String name(String id) {
        return toppings.stream().filter(t -> t.id().equals(id)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown topping: " + id)).name();
    }
}
