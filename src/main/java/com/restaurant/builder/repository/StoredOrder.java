package com.restaurant.builder.repository;

import com.restaurant.builder.product.Order;
import com.restaurant.builder.application.dto.OrderRequest;
import com.restaurant.observer.subject.OrderProgress;

public record StoredOrder(Order order, OrderProgress progress, OrderRequest request) { }
