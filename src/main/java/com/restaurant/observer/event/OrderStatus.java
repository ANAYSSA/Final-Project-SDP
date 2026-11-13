package com.restaurant.observer.event;

public enum OrderStatus {
    PLACED, PREPARING, READY, COMPLETED;
    public OrderStatus next() {
        return switch (this) {
            case PLACED -> PREPARING;
            case PREPARING -> READY;
            case READY -> COMPLETED;
            case COMPLETED -> throw new IllegalStateException("This order is already completed");
        };
    }
}
