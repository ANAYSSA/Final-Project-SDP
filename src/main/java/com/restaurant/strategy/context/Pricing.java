package com.restaurant.strategy.context;

import java.math.BigDecimal;

public record Pricing(BigDecimal subtotal, BigDecimal discount, BigDecimal total, String currency) { }
