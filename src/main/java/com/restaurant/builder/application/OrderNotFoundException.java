package com.restaurant.builder.application;

public final class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException() { super("Order not found in this session"); }
}
