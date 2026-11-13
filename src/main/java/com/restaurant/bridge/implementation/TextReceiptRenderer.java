package com.restaurant.bridge.implementation;

public final class TextReceiptRenderer implements ReceiptRenderer {
    public String render(ReceiptContent content) { return content.title() + "\n" + "=".repeat(40) + "\n" + String.join("\n", content.lines()) + "\n"; }
    public String contentType() { return "text/plain;charset=UTF-8"; }
}
