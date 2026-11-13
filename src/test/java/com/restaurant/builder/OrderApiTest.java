package com.restaurant.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restaurant.Main;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
class OrderApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private ObjectNode request() {
        ObjectNode request = json.createObjectNode();
        request.put("customerName", "Anuar");
        request.put("fulfillment", "TAKEAWAY");
        request.put("discountCode", "PICKUP");
        request.put("note", "No rush");
        request.put("requestId", UUID.randomUUID().toString());
        ObjectNode item = request.putArray("items").addObject();
        item.put("menuItemId", "margherita");
        item.put("quantity", 2);
        item.putArray("toppings").add("cheese").add("olives");
        return request;
    }
    private JsonNode place(ObjectNode request, MockHttpSession session) throws Exception {
        return json.readTree(mvc.perform(post("/api/orders").session(session).contentType(MediaType.APPLICATION_JSON).content(request.toString()))
            .andExpect(status().isCreated()).andExpect(header().string("Cache-Control", "no-store")).andReturn().getResponse().getContentAsString());
    }
    @Test void quoteMatchesCreatedOrderAndRetryDoesNotDuplicateIt() throws Exception {
        ObjectNode request = request();
        ObjectNode quoteRequest = request.deepCopy();
        quoteRequest.remove(java.util.List.of("customerName", "note", "requestId"));
        JsonNode quote = json.readTree(mvc.perform(post("/api/orders/quote").contentType(MediaType.APPLICATION_JSON).content(quoteRequest.toString()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        MockHttpSession session = new MockHttpSession();
        JsonNode first = place(request, session);
        JsonNode repeated = place(request, session);
        assertThat(first.get("id")).isEqualTo(repeated.get("id"));
        assertThat(first.get("total").decimalValue()).isEqualByComparingTo("4240.00");
        assertThat(first.get("total")).isEqualTo(quote.get("total"));
        assertThat(first.get("subtotal")).isEqualTo(quote.get("subtotal"));
        assertThat(first.get("events")).hasSize(2);
        mvc.perform(get("/api/orders").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        request.put("note", "Changed request");
        mvc.perform(post("/api/orders").session(session).contentType(MediaType.APPLICATION_JSON).content(request.toString()))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.message").isString());
    }
    @Test void otherSessionsCannotReadOrModifyAnOrderOrItsReceipt() throws Exception {
        MockHttpSession owner = new MockHttpSession();
        String id = place(request(), owner).get("id").asText();
        MockHttpSession other = new MockHttpSession();
        mvc.perform(get("/api/orders").session(other)).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/orders/" + id).session(other)).andExpect(status().isNotFound());
        mvc.perform(get("/api/orders/" + id + "/receipt").session(other)).andExpect(status().isNotFound());
        mvc.perform(post("/api/orders/" + id + "/advance").session(other).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/orders/" + id).session(owner)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PLACED"));
    }
    @Test void lifecycleAndAllFourReceiptCombinationsAreOperational() throws Exception {
        MockHttpSession session = new MockHttpSession();
        ObjectNode request = request();
        request.put("customerName", "<script>x</script>");
        request.put("note", "<img src=x>");
        String id = place(request, session).get("id").asText();
        int eventCount = 2;
        for (String next : new String[]{"PREPARING", "READY", "COMPLETED"}) {
            eventCount += 2;
            mvc.perform(post("/api/orders/" + id + "/advance").session(session).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(next)).andExpect(jsonPath("$.events.length()").value(eventCount));
        }
        mvc.perform(post("/api/orders/" + id + "/advance").session(session).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isConflict());
        for (String type : new String[]{"customer", "kitchen"}) for (String format : new String[]{"text", "html"}) {
            String receipt = mvc.perform(get("/api/orders/" + id + "/receipt").session(session).param("format", format).param("type", type))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(receipt).contains("Margherita", "COMPLETED");
            if (format.equals("html")) assertThat(receipt).doesNotContain("<script>", "<img ").contains("&lt;img");
            if (type.equals("kitchen")) assertThat(receipt).doesNotContain("4240.00");
        }
        mvc.perform(get("/api/orders/" + id + "/receipt").session(session).param("format", "pdf")).andExpect(status().isBadRequest());
    }
    static Stream<String> malformedBodies() {
        return Stream.of("{", "null", "{}", "{\"items\":null}",
            "{\"items\":[null],\"fulfillment\":\"TAKEAWAY\",\"discountCode\":\"NONE\"}");
    }
    @ParameterizedTest @MethodSource("malformedBodies")
    void malformedQuotesAreClientErrors(String body) throws Exception {
        mvc.perform(post("/api/orders/quote").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
    }
    @Test void invalidBusinessInputsAndIntegerOverflowAreRejected() throws Exception {
        for (String badQuantity : new String[]{"0", "-1", "11", "2147483647", "2147483648", "1.5", "null"}) {
            ObjectNode request = request();
            ((ObjectNode)request.get("items").get(0)).set("quantity", json.readTree(badQuantity));
            mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(request.toString())).andExpect(status().isBadRequest());
        }
        ObjectNode request = request();
        request.put("fulfillment", "DINE_IN").put("tableNumber", 1);
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(request.toString())).andExpect(status().isBadRequest());
        request.put("discountCode", "NONE").remove("tableNumber");
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(request.toString())).andExpect(status().isBadRequest());
        request = request();
        ((ObjectNode)request.get("items").get(0)).put("menuItemId", "lemonade");
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(request.toString())).andExpect(status().isBadRequest());
        request = request();
        ((ObjectNode)request.get("items").get(0)).putArray("toppings").add("cheese").add("cheese");
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(request.toString())).andExpect(status().isBadRequest());
        mvc.perform(get("/api/orders/not-a-uuid")).andExpect(status().isBadRequest());
    }
    @Test void sessionOrderStorageHasAnExplicitBound() throws Exception {
        MockHttpSession session = new MockHttpSession();
        for (int i = 0; i < 20; i++) place(request(), session);
        mvc.perform(post("/api/orders").session(session).contentType(MediaType.APPLICATION_JSON).content(request().toString()))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("Demo limit reached: 20 orders per session"));
    }
    @Test void simpleCrossOriginFormWritesAreRejected() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_FORM_URLENCODED).content("customerName=Alice"))
            .andExpect(status().isUnsupportedMediaType()).andExpect(jsonPath("$.message").isString());
    }
}
