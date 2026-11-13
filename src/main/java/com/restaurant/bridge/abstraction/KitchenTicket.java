package com.restaurant.bridge.abstraction;

import com.restaurant.bridge.implementation.*;
import com.restaurant.builder.product.*;
import com.restaurant.observer.event.OrderStatus;
import java.util.ArrayList;
import java.util.List;

public final class KitchenTicket extends ReceiptDocument {
    public KitchenTicket(ReceiptRenderer renderer) { super(renderer); }
    protected ReceiptContent content(Order order, OrderStatus status) {
        List<String> lines = new ArrayList<>();
        lines.add("Ticket: " + order.id());
        lines.add("Service: " + order.fulfillment() + (order.tableNumber() == null ? "" : " · Table " + order.tableNumber()));
        lines.add("Status: " + status);
        for (OrderLine item : order.items()) lines.add(item.quantity() + " × " + item.meal().name());
        lines.add("Preparation note: " + (order.note().isBlank() ? "None" : order.note()));
        return new ReceiptContent("Hearth & Table · Kitchen Ticket", lines);
    }
}
