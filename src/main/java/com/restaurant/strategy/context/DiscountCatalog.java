package com.restaurant.strategy.context;

import com.restaurant.strategy.api.DiscountStrategy;
import com.restaurant.strategy.concrete.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registry of selectable strategies. Each strategy is registered once, keyed by its own code, with its menu metadata. */
public final class DiscountCatalog {
    public record DiscountOption(String id, String name, String description) { }
    private record Registration(DiscountStrategy strategy, DiscountOption option) { }
    private final Map<String, Registration> registrations = new LinkedHashMap<>();
    public DiscountCatalog() {
        register(new NoDiscount(), "No discount", "Regular menu prices");
        register(new StudentDiscount(), "Student - 10%", "Demo student discount");
        register(new PickupDiscount(), "Pickup - 20%", "Available for takeaway orders only");
        register(new FamilyDiscount(), "Family - 15%", "Dine-in or takeaway orders with a subtotal of at least 6000 KZT");
    }
    private void register(DiscountStrategy strategy, String name, String description) {
        if (registrations.putIfAbsent(strategy.code(), new Registration(strategy, new DiscountOption(strategy.code(), name, description))) != null)
            throw new IllegalStateException("Duplicate discount code: " + strategy.code());
    }
    public DiscountStrategy find(String code) {
        Registration registration = registrations.get(code);
        if (registration == null) throw new IllegalArgumentException("Unknown discount code: " + code);
        return registration.strategy();
    }
    public List<DiscountOption> items() { return registrations.values().stream().map(Registration::option).toList(); }
}
