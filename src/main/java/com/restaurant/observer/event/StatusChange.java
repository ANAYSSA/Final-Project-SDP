package com.restaurant.observer.event;

import java.time.Instant;
import java.util.UUID;

public record StatusChange(UUID orderId, OrderStatus status, Instant occurredAt) { }
