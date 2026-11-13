package com.restaurant.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.bridge.abstraction.*;
import com.restaurant.bridge.implementation.*;
import com.restaurant.builder.product.*;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.observer.event.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class JsonReceiptRendererTest {
    private static final String NAME = "Ana \"Mama\" O'Neil";
    private static final String NOTE = "Line one\nSay \"no onions\" \\ <b>please</b>";
    private final ObjectMapper json = new ObjectMapper();

    private Order order() {
        return new Order.Builder().customer(NAME).fulfillment(Fulfillment.DINE_IN).tableNumber(7).note(NOTE)
            .addItem(new OrderLine(new MenuCatalog().create("pesto"), 2, List.of())).build();
    }
    private List<String> lines(JsonNode receipt) {
        List<String> lines = new ArrayList<>();
        receipt.get("lines").forEach(line -> lines.add(line.asText()));
        return lines;
    }

    @Test void oneRendererServesBothDocumentPurposesThroughTheCommonContract() throws Exception {
        ReceiptRenderer renderer = new JsonReceiptRenderer();
        Order order = order();
        List<ReceiptDocument> documents = List.of(new CustomerReceipt(renderer), new KitchenTicket(renderer));
        for (ReceiptDocument document : documents) {
            assertThat(document.contentType()).isEqualTo("application/json");
            JsonNode receipt = json.readTree(document.render(order, OrderStatus.PREPARING));
            assertThat(receipt.get("title").isTextual()).isTrue();
            assertThat(receipt.get("lines").isArray()).isTrue();
            assertThat(lines(receipt)).contains("Status: PREPARING", "Service: DINE_IN · Table 7");
        }
    }
    @Test void customerReceiptRoundTripsQuotesNewlinesAndMarkupExactly() throws Exception {
        String body = new CustomerReceipt(new JsonReceiptRenderer()).render(order(), OrderStatus.PLACED);
        assertThat(body).doesNotContain("\n");
        JsonNode receipt = json.readTree(body);
        assertThat(receipt.get("title").asText()).isEqualTo("Hearth & Table · Customer Receipt");
        assertThat(lines(receipt)).contains("Guest: " + NAME, "Note: " + NOTE, "Total: 5200.00 KZT", "2 × Basil Pesto — 5200.00 KZT");
    }
    @Test void kitchenJsonKeepsKitchenContentRules() throws Exception {
        JsonNode receipt = json.readTree(new KitchenTicket(new JsonReceiptRenderer()).render(order(), OrderStatus.PLACED));
        assertThat(receipt.get("title").asText()).isEqualTo("Hearth & Table · Kitchen Ticket");
        List<String> lines = lines(receipt);
        assertThat(lines).contains("2 × Basil Pesto", "Preparation note: " + NOTE);
        assertThat(String.join("\n", lines)).doesNotContain("KZT", "Total:", "Subtotal:", "Discount", "5200.00", NAME);
    }
    @Test void jsonCarriesExactlyTheSameContentAsTheTextRendererForEachPurpose() throws Exception {
        Order order = order();
        for (boolean kitchen : new boolean[]{false, true}) {
            ReceiptDocument asText = kitchen ? new KitchenTicket(new TextReceiptRenderer()) : new CustomerReceipt(new TextReceiptRenderer());
            ReceiptDocument asJson = kitchen ? new KitchenTicket(new JsonReceiptRenderer()) : new CustomerReceipt(new JsonReceiptRenderer());
            JsonNode receipt = json.readTree(asJson.render(order, OrderStatus.READY));
            String expectedText = receipt.get("title").asText() + "\n" + "=".repeat(40) + "\n" + String.join("\n", lines(receipt)) + "\n";
            assertThat(asText.render(order, OrderStatus.READY)).isEqualTo(expectedText);
        }
    }
    @Test void rendererEncodesArbitraryContentSafely() throws Exception {
        List<String> tricky = List.of("\"}, {\"injected\": true", "back\\slash", "tab\tand\nnewline", "  ", "</script>");
        JsonNode receipt = json.readTree(new JsonReceiptRenderer().render(new ReceiptContent("T \"quoted\"", tricky)));
        assertThat(receipt.size()).isEqualTo(2);
        assertThat(receipt.get("title").asText()).isEqualTo("T \"quoted\"");
        assertThat(lines(receipt)).containsExactlyElementsOf(tricky);
    }
}
