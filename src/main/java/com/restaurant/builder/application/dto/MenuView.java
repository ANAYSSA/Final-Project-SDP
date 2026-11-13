package com.restaurant.builder.application.dto;

import com.restaurant.factory.product.MenuDefinition;
import com.restaurant.decorator.catalog.ToppingCatalog.Topping;
import com.restaurant.strategy.context.DiscountCatalog.DiscountOption;
import java.util.List;

public record MenuView(List<MenuDefinition> items, List<Topping> toppings, List<DiscountOption> discounts, String currency) { }
