package com.restaurant.decorator.component;

import java.math.BigDecimal;

/** Single source of names and prices for decorators and the menu API. */
public enum ToppingType {
    CHEESE("cheese", "Extra cheese", "400.00"),
    MUSHROOMS("mushrooms", "Mushrooms", "300.00"),
    OLIVES("olives", "Olives", "250.00"),
    HERBS("herbs", "Herbs", "150.00");
    private final String id;
    private final String displayName;
    private final BigDecimal price;
    ToppingType(String id, String displayName, String price) {
        this.id = id; this.displayName = displayName; this.price = new BigDecimal(price);
    }
    public String id() { return id; }
    public String displayName() { return displayName; }
    public BigDecimal price() { return price; }
}
