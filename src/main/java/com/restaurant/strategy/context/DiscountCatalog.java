package com.restaurant.strategy.context;

import com.restaurant.strategy.api.DiscountStrategy;
import com.restaurant.strategy.concrete.*;
import java.util.List;
import java.util.Map;

public final class DiscountCatalog {
    public record DiscountOption(String id, String name, String description) { }
    private final Map<String, DiscountStrategy> strategies = Map.of(
        "NONE", new NoDiscount(), "STUDENT", new StudentDiscount(), "PICKUP", new PickupDiscount());
    public DiscountStrategy find(String code) {
        DiscountStrategy strategy = strategies.get(code);
        if (strategy == null) throw new IllegalArgumentException("Unknown discount code: " + code);
        return strategy;
    }
    public List<DiscountOption> items() {
        return List.of(new DiscountOption("NONE", "No discount", "Regular menu prices"),
            new DiscountOption("STUDENT", "Student - 10%", "Demo student discount"),
            new DiscountOption("PICKUP", "Pickup - 20%", "Available for takeaway orders only"));
    }
}
