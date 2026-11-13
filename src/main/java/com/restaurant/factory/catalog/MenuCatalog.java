package com.restaurant.factory.catalog;

import com.restaurant.factory.creator.*;
import com.restaurant.factory.product.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Menu data and creator selection are kept outside concrete products. */
public final class MenuCatalog {
    private final List<MenuDefinition> menu = List.of(
        item("margherita", "Margherita", "San Marzano tomato, mozzarella, fresh basil", MealCategory.PIZZA, "2000.00"),
        item("pepperoni", "Pepperoni", "Tomato, mozzarella, pepperoni, oregano", MealCategory.PIZZA, "2500.00"),
        item("garden", "Garden Pizza", "Bell pepper, cherry tomato, zucchini, mozzarella", MealCategory.PIZZA, "2200.00"),
        item("alfredo", "Chicken Alfredo", "Creamy parmesan sauce, chicken, fettuccine", MealCategory.PASTA, "2800.00"),
        item("pesto", "Basil Pesto", "Basil pesto, parmesan, cherry tomatoes, penne", MealCategory.PASTA, "2600.00"),
        item("lemonade", "House Lemonade", "Fresh lemon, mint, sparkling water", MealCategory.DRINK, "700.00")
    );
    private final Map<MealCategory, MealFactory> factories = Map.of(
        MealCategory.PIZZA, new PizzaFactory(), MealCategory.PASTA, new PastaFactory(), MealCategory.DRINK, new DrinkFactory());
    private static MenuDefinition item(String id, String name, String description, MealCategory category, String price) {
        return new MenuDefinition(id, name, description, category, new BigDecimal(price));
    }
    public List<MenuDefinition> items() { return menu; }
    public Meal create(String id) {
        MenuDefinition definition = menu.stream().filter(item -> item.id().equals(id)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown menu item: " + id));
        return factories.get(definition.category()).create(definition);
    }
}
