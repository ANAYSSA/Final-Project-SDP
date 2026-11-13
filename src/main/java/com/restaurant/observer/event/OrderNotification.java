package com.restaurant.observer.event;

import java.time.Instant;

public record OrderNotification(OrderStatus status, String message, String audience, Instant occurredAt) { }
