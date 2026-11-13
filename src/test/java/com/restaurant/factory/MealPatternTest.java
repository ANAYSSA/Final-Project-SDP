package com.restaurant.factory;

import com.restaurant.decorator.catalog.ToppingCatalog;
import com.restaurant.decorator.concrete.*;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.factory.creator.PizzaFactory;
import com.restaurant.factory.product.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class MealPatternTest {
    private final MenuCatalog menu = new MenuCatalog();
    @Test void factoryMethodsCreateDifferentProductTypes() {
        assertThat(menu.create("margherita")).isInstanceOf(Pizza.class);
        assertThat(menu.create("alfredo")).isInstanceOf(Pasta.class);
        assertThat(menu.create("lemonade")).isInstanceOf(Drink.class);
        assertThatThrownBy(() -> new PizzaFactory().create(menu.items().getLast())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> menu.create("missing")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void nestedDecoratorsPreserveBaseMealAndAddPrices() {
        Meal base = menu.create("margherita");
        Meal decorated = new Olives(new Mushrooms(new ExtraCheese(base)));
        assertThat(base.price()).isEqualByComparingTo("2000.00");
        assertThat(decorated.price()).isEqualByComparingTo("2950.00");
        assertThat(decorated.category()).isEqualTo(MealCategory.PIZZA);
        assertThat(decorated.name()).contains("Extra cheese", "Mushrooms", "Olives");
        assertThatThrownBy(() -> new ExtraCheese(menu.create("lemonade"))).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void publicToppingPricesMatchTheirDecorators() {
        ToppingCatalog toppings = new ToppingCatalog();
        Meal base = menu.create("pesto");
        toppings.items().forEach(t -> assertThat(toppings.add(base, t.id()).price().subtract(base.price())).isEqualByComparingTo(t.price()));
    }
}
