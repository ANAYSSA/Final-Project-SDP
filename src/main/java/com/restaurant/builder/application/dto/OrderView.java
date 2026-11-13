package com.restaurant.builder.application.dto;

import com.restaurant.builder.product.Fulfillment;
import com.restaurant.builder.product.Order;
import com.restaurant.observer.event.*;
import com.restaurant.observer.subject.OrderProgress;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** HTTP representation avoids exposing mutable subjects or implementation classes. */
public record OrderView(UUID id, String customerName, Fulfillment fulfillment, Integer tableNumber, String note,
                        OrderStatus status, Instant createdAt, List<LineView> items, BigDecimal subtotal,
                        BigDecimal discount, BigDecimal total, String currency, List<OrderNotification> events) {
    public record LineView(String name, int quantity, BigDecimal unitPrice, BigDecimal lineTotal, List<String> toppings) { }
    public static OrderView of(Order order, OrderProgress progress) {
        synchronized (progress) {
            return new OrderView(order.id(), order.customerName(), order.fulfillment(), order.tableNumber(), order.note(),
                progress.status(), order.createdAt(), order.items().stream().map(line -> new LineView(line.meal().name(),
                    line.quantity(), line.meal().price(), line.lineTotal(), line.toppings())).toList(),
                order.pricing().subtotal(), order.pricing().discount(), order.pricing().total(), order.pricing().currency(), progress.notifications());
        }
    }
}
