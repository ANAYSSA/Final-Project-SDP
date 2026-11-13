package com.restaurant.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restaurant.Main;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
class FamilyDiscountApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private ObjectNode quoteRequest(String discount, String fulfillment, String menuItemId, int quantity, String... toppings) {
        ObjectNode request = json.createObjectNode();
        request.put("discountCode", discount);
        request.put("fulfillment", fulfillment);
        ObjectNode item = request.putArray("items").addObject();
        item.put("menuItemId", menuItemId);
        item.put("quantity", quantity);
        ArrayNode array = item.putArray("toppings");
        for (String topping : toppings) array.add(topping);
        return request;
    }
    private ObjectNode orderRequest(ObjectNode quote) {
        ObjectNode order = quote.deepCopy();
        order.put("customerName", "Akmaral");
        order.put("requestId", UUID.randomUUID().toString());
        if (order.get("fulfillment").asText().equals("DINE_IN")) order.put("tableNumber", 4);
        return order;
    }
    private JsonNode quote(ObjectNode request) throws Exception {
        return json.readTree(mvc.perform(post("/api/orders/quote").contentType(MediaType.APPLICATION_JSON).content(request.toString()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    private JsonNode place(ObjectNode request, MockHttpSession session) throws Exception {
        return json.readTree(mvc.perform(post("/api/orders").session(session).contentType(MediaType.APPLICATION_JSON).content(request.toString()))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }
    private void assertQuoteAndOrderAgree(ObjectNode quoteRequest, String subtotal, String discount, String total) throws Exception {
        JsonNode quote = quote(quoteRequest);
        JsonNode order = place(orderRequest(quoteRequest), new MockHttpSession());
        for (JsonNode pricing : new JsonNode[]{quote, order}) {
            assertThat(pricing.get("subtotal").decimalValue()).isEqualByComparingTo(subtotal);
            assertThat(pricing.get("discount").decimalValue()).isEqualByComparingTo(discount);
            assertThat(pricing.get("total").decimalValue()).isEqualByComparingTo(total);
        }
        assertThat(order.get("subtotal")).isEqualTo(quote.get("subtotal"));
        assertThat(order.get("discount")).isEqualTo(quote.get("discount"));
        assertThat(order.get("total")).isEqualTo(quote.get("total"));
    }

    @Test void menuListsFamilyDiscountNextToExistingModes() throws Exception {
        mvc.perform(get("/api/menu")).andExpect(status().isOk())
            .andExpect(jsonPath("$.discounts[*].id").value(contains("NONE", "STUDENT", "PICKUP", "FAMILY")))
            .andExpect(jsonPath("$.discounts[3].name").value("Family - 15%"))
            .andExpect(jsonPath("$.discounts[3].description").value(containsString("6000 KZT")));
    }
    @Test void dineInFamilyAtThresholdIncludesToppingsAndQuantity() throws Exception {
        // Basil Pesto 2600 + Extra cheese 400 = 3000 per unit, x2 = 6000
        assertQuoteAndOrderAgree(quoteRequest("FAMILY", "DINE_IN", "pesto", 2, "cheese"), "6000.00", "900.00", "5100.00");
    }
    @Test void takeawayFamilyLargeOrder() throws Exception {
        // Margherita 2000 x5 = 10000
        assertQuoteAndOrderAgree(quoteRequest("FAMILY", "TAKEAWAY", "margherita", 5), "10000.00", "1500.00", "8500.00");
    }
    @Test void familyBelowThresholdIsRejectedAndNoOrderIsCreated() throws Exception {
        ObjectNode belowThreshold = quoteRequest("FAMILY", "TAKEAWAY", "alfredo", 2); // 2800 x2 = 5600
        mvc.perform(post("/api/orders/quote").contentType(MediaType.APPLICATION_JSON).content(belowThreshold.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("at least 6000.00 KZT")));
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/orders").session(session).contentType(MediaType.APPLICATION_JSON).content(orderRequest(belowThreshold).toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("5600.00")));
        mvc.perform(get("/api/orders").session(session)).andExpect(jsonPath("$.length()").value(0));
    }
    @Test void combinedDiscountCodesAreRejected() throws Exception {
        for (String combined : new String[]{"FAMILY,STUDENT", "FAMILY+PICKUP"})
            mvc.perform(post("/api/orders/quote").contentType(MediaType.APPLICATION_JSON)
                    .content(quoteRequest(combined, "TAKEAWAY", "margherita", 5).toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Unknown discount code: " + combined));
    }
    @ParameterizedTest
    @CsvSource({"NONE, 5300.00, 0.00, 5300.00", "STUDENT, 5300.00, 530.00, 4770.00", "PICKUP, 5300.00, 1060.00, 4240.00"})
    void existingModesStillWorkInQuoteAndOrder(String code, String subtotal, String discount, String total) throws Exception {
        // Margherita 2000 + cheese 400 + olives 250 = 2650, x2 = 5300
        assertQuoteAndOrderAgree(quoteRequest(code, "TAKEAWAY", "margherita", 2, "cheese", "olives"), subtotal, discount, total);
    }
}
