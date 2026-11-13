package com.restaurant.decorator;

import com.restaurant.builder.product.OrderLine;
import com.restaurant.decorator.catalog.ToppingCatalog;
import com.restaurant.decorator.component.ToppingType;
import com.restaurant.decorator.concrete.*;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.factory.product.Meal;
import com.restaurant.factory.product.MealCategory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class HerbsDecoratorTest {
    private final MenuCatalog menu = new MenuCatalog();

    @Test void herbsAddsItsPricePerUnitWithoutChangingTheBaseMeal() {
        Meal base = menu.create("margherita");
        Meal decorated = new HerbsDecorator(base);
        assertThat(decorated.price()).isEqualByComparingTo("2150.00");
        assertThat(new OrderLine(decorated, 2, List.of("Herbs")).lineTotal()).isEqualByComparingTo("4300.00");
        assertThat(decorated.id()).isEqualTo(base.id());
        assertThat(decorated.category()).isEqualTo(MealCategory.PIZZA);
        assertThat(decorated.name()).isEqualTo("Margherita + Herbs");
        assertThat(decorated.description()).isEqualTo(base.description() + ", herbs");
        assertThat(base.name()).isEqualTo("Margherita");
        assertThat(base.price()).isEqualByComparingTo("2000.00");
    }

    @Test void herbsAndCheeseComposeInEitherWrappingOrder() {
        Meal base = menu.create("margherita");
        for (Meal decorated : List.of(new HerbsDecorator(new ExtraCheese(base)), new ExtraCheese(new HerbsDecorator(base)))) {
            assertThat(decorated.price()).isEqualByComparingTo("2550.00");
            assertThat(decorated.name()).contains("Margherita", "Herbs", "Extra cheese");
            assertThat(decorated.id()).isEqualTo(base.id());
            assertThat(decorated.category()).isEqualTo(base.category());
        }
    }

    @Test void herbsComposesWithEveryExistingDecoratorThroughTheMealContract() {
        Meal base = menu.create("margherita");
        Meal decorated = new Olives(new Mushrooms(new ExtraCheese(new HerbsDecorator(base))));
        assertThat(decorated.price()).isEqualByComparingTo("3100.00");
        assertThat(decorated.name()).contains("Herbs", "Extra cheese", "Mushrooms", "Olives");
        assertThat(decorated.id()).isEqualTo(base.id());
        assertThat(decorated.category()).isEqualTo(base.category());
    }

    @ParameterizedTest @ValueSource(strings = {"margherita", "alfredo", "pesto"})
    void catalogUsesTheSameMetadataForPizzaAndPasta(String mealId) {
        ToppingCatalog toppings = new ToppingCatalog();
        Meal base = menu.create(mealId);
        Meal decorated = toppings.add(base, "herbs");
        assertThat(decorated).isInstanceOf(HerbsDecorator.class);
        assertThat(decorated.price().subtract(base.price())).isEqualByComparingTo(ToppingType.HERBS.price());
        assertThat(decorated.id()).isEqualTo(base.id());
        assertThat(decorated.category()).isEqualTo(base.category());
        assertThat(toppings.items()).filteredOn(t -> t.id().equals("herbs")).singleElement().satisfies(t -> {
            assertThat(t.name()).isEqualTo(ToppingType.HERBS.displayName());
            assertThat(t.price()).isEqualByComparingTo("150.00");
        });
    }

    @Test void drinksRejectHerbsThroughBothDirectAndCatalogConstruction() {
        Meal drink = menu.create("lemonade");
        assertThatThrownBy(() -> new HerbsDecorator(drink)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Toppings are available for pizza and pasta only");
        assertThatThrownBy(() -> new ToppingCatalog().add(drink, "herbs")).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Toppings are available for pizza and pasta only");
    }
}
