package com.restaurant.observer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restaurant.Main;
import com.restaurant.builder.application.OrderService;
import com.restaurant.builder.application.dto.ItemRequest;
import com.restaurant.builder.application.dto.OrderRequest;
import com.restaurant.builder.application.dto.OrderView;
import com.restaurant.builder.product.Fulfillment;
import com.restaurant.builder.repository.OrderBook;
import com.restaurant.decorator.catalog.ToppingCatalog;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.strategy.context.DiscountCatalog;
import com.restaurant.strategy.context.PricingCalculator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
class OperationsDashboardApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private ObjectNode request() {
        ObjectNode request = json.createObjectNode();
        request.put("customerName", "PRIVATE_CUSTOMER_Mukhammad");
        request.put("fulfillment", "DINE_IN");
        request.put("tableNumber", 29);
        request.put("discountCode", "NONE");
        request.put("note", "PRIVATE_NOTE_keep_allergy_context_here");
        request.put("requestId", UUID.randomUUID().toString());
        var items = request.putArray("items");
        ObjectNode pizza = items.addObject();
        pizza.put("menuItemId", "margherita").put("quantity", 2);
        pizza.putArray("toppings");
        ObjectNode pasta = items.addObject();
        pasta.put("menuItemId", "pesto").put("quantity", 3);
        pasta.putArray("toppings");
        return request;
    }

    private JsonNode place(ObjectNode request, MockHttpSession session) throws Exception {
        return json.readTree(mvc.perform(post("/api/orders").session(session)
            .contentType(MediaType.APPLICATION_JSON).content(request.toString()))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private JsonNode read(String id, MockHttpSession session) throws Exception {
        return json.readTree(mvc.perform(get("/api/orders/" + id).session(session))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
            .andReturn().getResponse().getContentAsString());
    }

    private JsonNode advance(String id, MockHttpSession session) throws Exception {
        return json.readTree(mvc.perform(post("/api/orders/" + id + "/advance").session(session)
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private void assertProjection(JsonNode order, List<String> statuses) {
        JsonNode operations = order.get("operationsEvents");
        JsonNode existingEvents = order.get("events");
        assertThat(operations).hasSize(statuses.size());
        assertThat(existingEvents).hasSize(statuses.size() * 2);
        String reference = order.get("id").asText().substring(0, 8).toUpperCase(Locale.ROOT);
        for (int index = 0; index < statuses.size(); index++) {
            JsonNode event = operations.get(index);
            assertThat(event.get("audience").asText()).isEqualTo("OPERATIONS");
            assertThat(event.get("status").asText()).isEqualTo(statuses.get(index));
            assertThat(event.get("message").asText())
                .isEqualTo("Order " + reference + " | " + statuses.get(index) + " | Items: 5");
            assertThat(existingEvents.get(index * 2).get("audience").asText()).isEqualTo("CUSTOMER");
            assertThat(existingEvents.get(index * 2 + 1).get("audience").asText()).isEqualTo("KITCHEN");
            for (int legacyIndex = index * 2; legacyIndex < index * 2 + 2; legacyIndex++) {
                assertThat(existingEvents.get(legacyIndex).get("status")).isEqualTo(event.get("status"));
                assertThat(existingEvents.get(legacyIndex).get("occurredAt")).isEqualTo(event.get("occurredAt"));
            }
        }
    }

    @Test void eachSuccessfulPublicationAddsOneOperationsEventAndRejectedAdvanceAddsNone() throws Exception {
        MockHttpSession session = new MockHttpSession();
        JsonNode order = place(request(), session);
        String id = order.get("id").asText();
        List<String> statuses = new ArrayList<>(List.of("PLACED"));
        assertProjection(order, statuses);
        for (String next : List.of("PREPARING", "READY", "COMPLETED")) {
            order = advance(id, session);
            statuses.add(next);
            assertThat(order.get("status").asText()).isEqualTo(next);
            assertProjection(order, statuses);
        }
        mvc.perform(post("/api/orders/" + id + "/advance").session(session)
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isConflict());
        assertThat(read(id, session)).isEqualTo(order);
    }

    @Test void idempotentRetriesAndConflictingRequestsCannotRepublishNotifications() throws Exception {
        MockHttpSession session = new MockHttpSession();
        ObjectNode request = request();
        JsonNode placed = place(request, session);
        assertThat(place(request, session)).isEqualTo(placed);
        String id = placed.get("id").asText();
        JsonNode preparing = advance(id, session);
        assertThat(place(request, session)).isEqualTo(preparing);
        assertProjection(preparing, List.of("PLACED", "PREPARING"));
        JsonNode orders = json.readTree(mvc.perform(get("/api/orders").session(session))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0)).isEqualTo(preparing);

        request.put("note", "Different payload with the same requestId");
        mvc.perform(post("/api/orders").session(session).contentType(MediaType.APPLICATION_JSON)
            .content(request.toString())).andExpect(status().isConflict());
        assertThat(read(id, session)).isEqualTo(preparing);
    }

    @Test void operationsProjectionContainsOnlyPublicEventFieldsAndAggregateQuantity() throws Exception {
        MockHttpSession session = new MockHttpSession(null, "PRIVATE_SESSION_IDENTIFIER_TOKEN");
        ObjectNode request = request();
        JsonNode order = place(request, session);
        String id = order.get("id").asText();
        for (int step = 0; step < 3; step++) order = advance(id, session);
        assertProjection(order, List.of("PLACED", "PREPARING", "READY", "COMPLETED"));
        JsonNode operations = order.get("operationsEvents");
        for (JsonNode event : operations) {
            List<String> fields = new ArrayList<>();
            event.fieldNames().forEachRemaining(fields::add);
            assertThat(fields).containsExactlyInAnyOrder("status", "message", "audience", "occurredAt");
        }
        assertThat(operations.toString()).doesNotContain(request.get("customerName").asText(),
            request.get("note").asText(), request.get("requestId").asText(), session.getId(),
            "customerName", "tableNumber", "note", "requestId", "sessionId", "token", "receipt");
    }

    @Test void anotherSessionCannotReadOperationsEventsOrMutateTheirOrder() throws Exception {
        MockHttpSession owner = new MockHttpSession();
        JsonNode placed = place(request(), owner);
        String id = placed.get("id").asText();
        MockHttpSession other = new MockHttpSession();
        mvc.perform(get("/api/orders").session(other)).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/orders/" + id).session(other)).andExpect(status().isNotFound());
        mvc.perform(get("/api/orders/" + id + "/receipt").session(other)).andExpect(status().isNotFound());
        mvc.perform(post("/api/orders/" + id + "/advance").session(other)
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isNotFound());
        assertThat(read(id, owner)).isEqualTo(placed);
        assertProjection(placed, List.of("PLACED"));
    }

    @Test void concurrentIdempotentRetriesPublishOnePlacementPerSubscriber() throws Exception {
        OrderService service = new OrderService(new MenuCatalog(), new ToppingCatalog(),
            new DiscountCatalog(), new PricingCalculator());
        OrderBook book = new OrderBook();
        OrderRequest request = new OrderRequest(List.of(new ItemRequest("margherita", 2, List.of()),
            new ItemRequest("pesto", 3, List.of())), "NONE", Fulfillment.TAKEAWAY,
            "Concurrent guest", null, "Private note", UUID.randomUUID());
        try (var executor = Executors.newFixedThreadPool(8)) {
            List<Callable<OrderView>> calls = IntStream.range(0, 32)
                .mapToObj(i -> (Callable<OrderView>) () -> service.place(request, book)).toList();
            var results = executor.invokeAll(calls);
            OrderView first = results.getFirst().get();
            for (var result : results) assertThat(result.get()).isEqualTo(first);
        }
        assertThat(service.list(book)).hasSize(1);
        assertProjection(json.valueToTree(service.list(book).getFirst()), List.of("PLACED"));
    }
}
