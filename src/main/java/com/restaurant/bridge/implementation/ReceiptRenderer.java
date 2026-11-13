package com.restaurant.bridge.implementation;

/** Implementor hierarchy is independent from receipt document types. */
public interface ReceiptRenderer {
    String render(ReceiptContent content);
    String contentType();
}
