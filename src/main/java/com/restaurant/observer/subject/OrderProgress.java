package com.restaurant.observer.subject;

import com.restaurant.observer.api.OrderObserver;
import com.restaurant.observer.event.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Subject owns lifecycle state and notifies every current subscriber. */
public final class OrderProgress {
    private final UUID orderId;
    private final Set<OrderObserver> observers = new LinkedHashSet<>();
    private final List<OrderNotification> notifications = new ArrayList<>();
    private OrderStatus status = OrderStatus.PLACED;
    private boolean announced;
    public OrderProgress(UUID orderId) { this.orderId = Objects.requireNonNull(orderId); }
    public synchronized void subscribe(OrderObserver observer) { observers.add(Objects.requireNonNull(observer)); }
    public synchronized void unsubscribe(OrderObserver observer) { observers.remove(observer); }
    public synchronized OrderStatus status() { return status; }
    public synchronized List<OrderNotification> notifications() { return List.copyOf(notifications); }
    public synchronized void announce() {
        if (announced) throw new IllegalStateException("Order placement was already announced");
        announced = true;
        notifyObservers();
    }
    public synchronized void advance() {
        if (!announced) throw new IllegalStateException("Announce placement before advancing an order");
        status = status.next();
        notifyObservers();
    }
    private void notifyObservers() {
        StatusChange change = new StatusChange(orderId, status, Instant.now());
        for (OrderObserver observer : List.copyOf(observers)) notifications.add(observer.onStatusChanged(change));
    }
}
