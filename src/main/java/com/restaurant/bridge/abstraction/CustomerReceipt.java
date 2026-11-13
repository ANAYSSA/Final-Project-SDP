package com.restaurant.bridge.abstraction;

import com.restaurant.bridge.implementation.*;
import com.restaurant.builder.product.*;
import com.restaurant.observer.event.OrderStatus;
import java.util.ArrayList;
import java.util.List;

public final class CustomerReceipt extends ReceiptDocument {
    public CustomerReceipt(ReceiptRenderer renderer) { super(renderer); }
    protected ReceiptContent content(Order order, OrderStatus status) {
        List<String> lines = new ArrayList<>();
        lines.add("Order: " + order.id());
        lines.add("Guest: " + order.customerName());
        lines.add("Service: " + order.fulfillment() + (order.tableNumber() == null ? "" : " · Table " + order.tableNumber()));
        lines.add("Status: " + status);
        for (OrderLine item : order.items()) lines.add(item.quantity() + " × " + item.meal().name() + " — " + item.lineTotal() + " KZT");
        lines.add("Subtotal: " + order.pricing().subtotal() + " KZT");
        lines.add("Discount (" + order.discountCode() + "): " + order.pricing().discount() + " KZT");
        lines.add("Total: " + order.pricing().total() + " KZT");
        if (!order.note().isBlank()) lines.add("Note: " + order.note());
        return new ReceiptContent("Hearth & Table · Customer Receipt", lines);
    }
}
