package com.restaurant.bridge.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.UncheckedIOException;

/**
 * Concrete implementor: encodes any document's content as {"title": ..., "lines": [...]}.
 * Jackson performs all escaping, so quotes, backslashes, newlines and markup in user text stay data.
 */
public final class JsonReceiptRenderer implements ReceiptRenderer {
    private static final ObjectMapper JSON = new ObjectMapper();
    public String render(ReceiptContent content) {
        ObjectNode receipt = JSON.createObjectNode();
        receipt.put("title", content.title());
        content.lines().forEach(receipt.putArray("lines")::add);
        try {
            return JSON.writeValueAsString(receipt);
        } catch (JsonProcessingException exception) {
            throw new UncheckedIOException("Receipt could not be encoded as JSON", exception);
        }
    }
    public String contentType() { return "application/json"; }
}
