package com.restaurant.bridge.abstraction;

import com.restaurant.bridge.implementation.*;
import com.restaurant.builder.product.Order;
import com.restaurant.observer.event.OrderStatus;
import java.util.Objects;

/** Abstraction: document purpose and output format can vary independently. */
public abstract class ReceiptDocument {
    private final ReceiptRenderer renderer;
    protected ReceiptDocument(ReceiptRenderer renderer) { this.renderer = Objects.requireNonNull(renderer); }
    public final String render(Order order, OrderStatus status) { return renderer.render(content(order, status)); }
    public final String contentType() { return renderer.contentType(); }
    protected abstract ReceiptContent content(Order order, OrderStatus status);
}
