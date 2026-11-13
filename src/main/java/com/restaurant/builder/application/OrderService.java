package com.restaurant.builder.application;

import com.restaurant.bridge.abstraction.*;
import com.restaurant.bridge.implementation.*;
import com.restaurant.builder.product.*;
import com.restaurant.builder.repository.*;
import com.restaurant.builder.application.dto.*;
import com.restaurant.decorator.catalog.ToppingCatalog;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.factory.product.Meal;
import com.restaurant.observer.concrete.*;
import com.restaurant.observer.subject.OrderProgress;
import com.restaurant.strategy.context.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Application coordination; pattern classes contain the domain algorithms. */
public final class OrderService {
    private final MenuCatalog menu;
    private final ToppingCatalog toppings;
    private final DiscountCatalog discounts;
    private final PricingCalculator pricing;
    public OrderService(MenuCatalog menu, ToppingCatalog toppings, DiscountCatalog discounts, PricingCalculator pricing) {
        this.menu = Objects.requireNonNull(menu);
        this.toppings = Objects.requireNonNull(toppings);
        this.discounts = Objects.requireNonNull(discounts);
        this.pricing = Objects.requireNonNull(pricing);
    }
    public MenuView menu() { return new MenuView(menu.items(), toppings.items(), discounts.items(), "KZT"); }
    public Pricing quote(QuoteRequest request) {
        requireQuote(request);
        return pricing.calculate(buildLines(request.items()), request.fulfillment(), discounts.find(request.discountCode()));
    }
    public OrderView place(OrderRequest request, OrderBook book) {
        if (request == null || request.requestId() == null) throw new IllegalArgumentException("A requestId UUID is required");
        requireQuote(request.quoteRequest());
        synchronized (book) {
            StoredOrder previous = book.findRequest(request.requestId());
            if (previous != null) {
                if (!previous.request().equals(request)) throw new IllegalStateException("This requestId was already used for a different order");
                return view(previous);
            }
            Order.Builder builder = new Order.Builder().customer(request.customerName()).fulfillment(request.fulfillment())
                .tableNumber(request.tableNumber()).note(request.note()).discount(discounts.find(request.discountCode()));
            buildLines(request.items()).forEach(builder::addItem);
            Order order = builder.build();
            OrderProgress progress = new OrderProgress(order.id());
            progress.subscribe(new CustomerObserver());
            progress.subscribe(new KitchenObserver());
            int itemQuantity = order.items().stream().mapToInt(OrderLine::quantity).sum();
            progress.subscribe(new OperationsDashboardObserver(itemQuantity));
            progress.announce();
            StoredOrder stored = new StoredOrder(order, progress, request);
            book.add(stored);
            return view(stored);
        }
    }
    public List<OrderView> list(OrderBook book) { return book.all().stream().map(this::view).toList(); }
    public OrderView get(UUID id, OrderBook book) { return view(find(id, book)); }
    public OrderView advance(UUID id, OrderBook book) {
        StoredOrder stored = find(id, book);
        synchronized (stored.progress()) {
            stored.progress().advance();
            return view(stored);
        }
    }
    public record RenderedReceipt(String body, String contentType) { }
    public RenderedReceipt receipt(UUID id, String format, String type, OrderBook book) {
        StoredOrder stored = find(id, book);
        ReceiptRenderer renderer = switch (format) {
            case "text" -> new TextReceiptRenderer();
            case "html" -> new HtmlReceiptRenderer();
            case "json" -> new JsonReceiptRenderer();
            default -> throw new IllegalArgumentException("Receipt format must be text, html or json");
        };
        ReceiptDocument document = switch (type) {
            case "customer" -> new CustomerReceipt(renderer);
            case "kitchen" -> new KitchenTicket(renderer);
            default -> throw new IllegalArgumentException("Receipt type must be customer or kitchen");
        };
        return new RenderedReceipt(document.render(stored.order(), stored.progress().status()), document.contentType());
    }
    private StoredOrder find(UUID id, OrderBook book) {
        StoredOrder stored = book.find(id);
        if (stored == null) throw new OrderNotFoundException();
        return stored;
    }
    private OrderView view(StoredOrder stored) { return OrderView.of(stored.order(), stored.progress()); }
    private void requireQuote(QuoteRequest request) {
        if (request == null || request.fulfillment() == null) throw new IllegalArgumentException("Choose dine-in or takeaway");
        if (request.discountCode() == null || request.discountCode().isBlank()) throw new IllegalArgumentException("A discount code is required");
    }
    private List<OrderLine> buildLines(List<ItemRequest> items) {
        if (items == null || items.isEmpty() || items.size() > 30) throw new IllegalArgumentException("An order must have 1 to 30 lines");
        List<OrderLine> lines = new ArrayList<>();
        for (ItemRequest item : items) {
            if (item == null || item.menuItemId() == null || item.menuItemId().isBlank()) throw new IllegalArgumentException("Every line needs a menu item");
            if (item.toppings() == null || item.toppings().size() > 3 || item.toppings().stream().anyMatch(t -> t == null || t.isBlank()))
                throw new IllegalArgumentException("Choose up to three valid toppings");
            if (new HashSet<>(item.toppings()).size() != item.toppings().size()) throw new IllegalArgumentException("A topping can only be added once per item");
            Meal meal = menu.create(item.menuItemId());
            for (String topping : item.toppings()) meal = toppings.add(meal, topping);
            lines.add(new OrderLine(meal, item.quantity(), item.toppings().stream().map(toppings::name).toList()));
        }
        return List.copyOf(lines);
    }
}
