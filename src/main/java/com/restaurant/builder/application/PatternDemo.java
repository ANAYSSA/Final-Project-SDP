package com.restaurant.builder.application;

import com.restaurant.builder.product.Fulfillment;
import com.restaurant.builder.repository.OrderBook;
import com.restaurant.builder.application.dto.*;
import com.restaurant.decorator.catalog.ToppingCatalog;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.strategy.context.*;
import java.util.List;
import java.util.UUID;

/** One reproducible, console-only walkthrough calls the same pipeline as the website. */
public final class PatternDemo {
    private PatternDemo() { }
    public static void run() {
        OrderService service = new OrderService(new MenuCatalog(), new ToppingCatalog(), new DiscountCatalog(), new PricingCalculator());
        OrderBook book = new OrderBook();
        OrderRequest request = new OrderRequest(List.of(new ItemRequest("margherita", 2, List.of("cheese", "olives"))),
            "PICKUP", Fulfillment.TAKEAWAY, "Demo guest", null, "Please slice for sharing", UUID.randomUUID());
        OrderView order = service.place(request, book);
        System.out.println("HEARTH & TABLE — six-pattern integrated demonstration");
        System.out.println("1. Factory Method: PizzaFactory created a Margherita product.");
        System.out.println("2. Decorator: ExtraCheese + Olives wrapped that Meal: " + order.items().getFirst().name());
        System.out.println("3. Builder: immutable order " + order.id() + " assembled and validated.");
        System.out.println("4. Strategy: pickup 20%: " + order.subtotal() + " - " + order.discount() + " = " + order.total() + " KZT.");
        System.out.println("5. Bridge: customer and kitchen documents share interchangeable text/HTML/JSON renderers.");
        for (String type : List.of("customer", "kitchen")) {
            System.out.println(service.receipt(order.id(), "text", type, book).body());
            if (!service.receipt(order.id(), "html", type, book).body().contains("<!doctype html>")) throw new IllegalStateException("HTML receipt failed");
            if (!service.receipt(order.id(), "json", type, book).body().startsWith("{\"title\":")) throw new IllegalStateException("JSON receipt failed");
        }
        System.out.println("6. Observer: kitchen, customer and operations receive every status transition.");
        for (int step = 0; step < 3; step++) order = service.advance(order.id(), book);
        order.events().forEach(event -> System.out.println(event.audience() + " / " + event.status() + ": " + event.message()));
        order.operationsEvents().forEach(event -> System.out.println(event.audience() + " / " + event.status() + ": " + event.message()));
        if (order.events().size() != 8 || order.operationsEvents().size() != 4
                || order.total().compareTo(new java.math.BigDecimal("4240.00")) != 0)
            throw new IllegalStateException("Integrated demonstration did not match expected results");
        System.out.println("PASS — all six patterns exercised successfully.");
    }
}
