package com.restaurant.observer.concrete;

import com.restaurant.observer.api.OrderObserver;
import com.restaurant.observer.event.OrderNotification;
import com.restaurant.observer.event.StatusChange;
import java.util.Locale;

/** Per-order operations projection; retains only the immutable aggregate quantity. */
public final class OperationsDashboardObserver implements OrderObserver {
    private final int itemQuantity;

    public OperationsDashboardObserver(int itemQuantity) {
        if (itemQuantity < 1) throw new IllegalArgumentException("Item quantity must be positive");
        this.itemQuantity = itemQuantity;
    }

    @Override public OrderNotification onStatusChanged(StatusChange change) {
        String reference = change.orderId().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String message = "Order " + reference + " | " + change.status() + " | Items: " + itemQuantity;
        return new OrderNotification(change.status(), message, "OPERATIONS", change.occurredAt());
    }
}
