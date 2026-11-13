package com.restaurant.bridge.implementation;

import java.util.List;

public record ReceiptContent(String title, List<String> lines) {
    public ReceiptContent { lines = List.copyOf(lines); }
}
