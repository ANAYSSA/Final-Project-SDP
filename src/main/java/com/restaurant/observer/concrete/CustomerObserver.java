package com.restaurant.observer.concrete;

import com.restaurant.observer.api.OrderObserver;
import com.restaurant.observer.event.*;

public final class CustomerObserver implements OrderObserver {
    @Override public OrderNotification onStatusChanged(StatusChange change) {
        String message = switch (change.status()) {
            case PLACED -> "Your order has been received.";
            case PREPARING -> "Your meal is being prepared.";
            case READY -> "Your order is ready. Enjoy!";
            case COMPLETED -> "Thank you for dining with Hearth & Table.";
        };
        return new OrderNotification(change.status(), message, "CUSTOMER", change.occurredAt());
    }
}
