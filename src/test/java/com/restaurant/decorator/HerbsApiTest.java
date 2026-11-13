package com.restaurant.decorator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restaurant.Main;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
class HerbsApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private ObjectNode request() {
        ObjectNode request = json.createObjectNode();
        request.put("customerName", "Mukhammad").put("fulfillment", "TAKEAWAY").put("discountCode", "NONE")
            .put("note", "").put("requestId", UUID.randomUUID().toString());
        ObjectNode item = request.putArray("items").addObject();
        item.put("menuItemId", "margherita").put("quantity", 2).putArray("toppings").add("herbs");
        return request;
    }

    private ObjectNode quoteRequest(ObjectNode request) {
        ObjectNode quote = request.deepCopy();
        quote.remove(List.of("customerName", "tableNumber", "note", "requestId"));
        return quote;
    }

    @Test void menuAdvertisesHerbsFromTheRegisteredToppingMetadata() throws Exception {
        JsonNode menu = json.readTree(mvc.perform(get("/api/menu")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        assertThat(menu.get("toppings")).filteredOn(topping -> topping.get("id").asText().equals("herbs"))
            .singleElement().satisfies(topping -> {
                assertThat(topping.get("name").asText()).isEqualTo("Herbs");
                assertThat(topping.get("price").decimalValue()).isEqualByComparingTo("150.00");
            });
    }

    @ParameterizedTest
    @CsvSource({"NONE, 0.00, 4300.00", "STUDENT, 430.00, 3870.00", "PICKUP, 860.00, 3440.00"})
    void herbsQuoteAndPlacedOrderAgreeIncludingExistingDiscounts(String code, String discount, String total) throws Exception {
        ObjectNode request = request().put("discountCode", code);
        JsonNode quote = json.readTree(mvc.perform(post("/api/orders/quote").contentType(MediaType.APPLICATION_JSON)
            .content(quoteRequest(request).toString())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        JsonNode order = json.readTree(mvc.perform(post("/api/orders").session(new MockHttpSession()).contentType(MediaType.APPLICATION_JSON)
            .content(request.toString())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        assertThat(order.get("subtotal").decimalValue()).isEqualByComparingTo("4300.00");
        assertThat(order.get("discount").decimalValue()).isEqualByComparingTo(discount);
        assertThat(order.get("total").decimalValue()).isEqualByComparingTo(total);
        for (String field : List.of("subtotal", "discount", "total", "currency")) assertThat(order.get(field)).isEqualTo(quote.get(field));
        assertThat(order.at("/items/0/name").asText()).isEqualTo("Margherita + Herbs");
        assertThat(order.at("/items/0/unitPrice").decimalValue()).isEqualByComparingTo("2150.00");
        assertThat(order.at("/items/0/lineTotal").decimalValue()).isEqualByComparingTo("4300.00");
        assertThat(order.at("/items/0/toppings/0").asText()).isEqualTo("Herbs");
    }

    @Test void herbsCanBeCombinedWithExistingToppingsOnPastaWithinTheThreeToppingLimit() throws Exception {
        ObjectNode request = request();
        ObjectNode item = (ObjectNode) request.at("/items/0");
        item.put("menuItemId", "pesto").put("quantity", 1).putArray("toppings").add("herbs").add("mushrooms").add("olives");
        mvc.perform(post("/api/orders/quote").contentType(MediaType.APPLICATION_JSON).content(quoteRequest(request).toString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(3300.0));
        mvc.perform(post("/api/orders").session(new MockHttpSession()).contentType(MediaType.APPLICATION_JSON).content(request.toString()))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(3300.0))
            .andExpect(jsonPath("$.items[0].name").value("Basil Pesto + Herbs + Mushrooms + Olives"));
    }

    static Stream<Arguments> invalidToppings() {
        return Stream.of(
            Arguments.of("margherita", "[\"herbs\",\"herbs\"]", "A topping can only be added once per item"),
            Arguments.of("margherita", "[\"cheese\",\"cheese\"]", "A topping can only be added once per item"),
            Arguments.of("margherita", "[\"herbs\",\"missing\"]", "Unknown topping: missing"),
            Arguments.of("margherita", "[\"HERBS\"]", "Unknown topping: HERBS"),
            Arguments.of("lemonade", "[\"herbs\"]", "Toppings are available for pizza and pasta only"),
            Arguments.of("margherita", "[\"herbs\",\"cheese\",\"mushrooms\",\"olives\"]", null),
            Arguments.of("margherita", "null", null),
            Arguments.of("margherita", "[null]", null),
            Arguments.of("margherita", "[\"\"]", null),
            Arguments.of("margherita", "[\"  \"]", null),
            Arguments.of("margherita", "[\"" + "x".repeat(41) + "\"]", null),
            Arguments.of("margherita", "[42]", null),
            Arguments.of("margherita", "[{}]", null),
            Arguments.of("margherita", "\"herbs\"", null)
        );
    }

    @ParameterizedTest @MethodSource("invalidToppings")
    void invalidToppingsFollowExistingRulesForQuoteAndPlacement(String mealId, String toppingJson, String expectedMessage) throws Exception {
        ObjectNode request = request();
        ObjectNode item = (ObjectNode) request.at("/items/0");
        item.put("menuItemId", mealId).set("toppings", json.readTree(toppingJson));
        MockHttpSession session = new MockHttpSession();
        for (String endpoint : List.of("/api/orders/quote", "/api/orders")) {
            ObjectNode body = endpoint.endsWith("quote") ? quoteRequest(request) : request;
            var result = mvc.perform(post(endpoint).session(session).contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
            if (expectedMessage != null) result.andExpect(jsonPath("$.message").value(expectedMessage));
        }
        mvc.perform(get("/api/orders").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }
}
