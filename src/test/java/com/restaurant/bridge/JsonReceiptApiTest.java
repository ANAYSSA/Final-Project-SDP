package com.restaurant.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restaurant.Main;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
class JsonReceiptApiTest {
    private static final String NAME = "Dana \"DJ\" O'Brien";
    private static final String NOTE = "Ring twice\nThen say \"hello\" <b>";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String place(MockHttpSession session) throws Exception {
        ObjectNode request = json.createObjectNode();
        request.put("customerName", NAME);
        request.put("fulfillment", "TAKEAWAY");
        request.put("discountCode", "STUDENT");
        request.put("note", NOTE);
        request.put("requestId", UUID.randomUUID().toString());
        ObjectNode item = request.putArray("items").addObject();
        item.put("menuItemId", "margherita");
        item.put("quantity", 2);
        item.putArray("toppings").add("cheese");
        return json.readTree(mvc.perform(post("/api/orders").session(session).contentType(MediaType.APPLICATION_JSON).content(request.toString()))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();
    }
    private JsonNode receipt(String id, String type, MockHttpSession session) throws Exception {
        String body = mvc.perform(get("/api/orders/" + id + "/receipt").session(session).param("format", "json").param("type", type))
            .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(header().string("Cache-Control", "no-store"))
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return json.readTree(body);
    }
    private List<String> lines(JsonNode receipt) {
        List<String> lines = new ArrayList<>();
        receipt.get("lines").forEach(line -> lines.add(line.asText()));
        return lines;
    }

    @Test void bothPurposesRenderParseableJsonWithUserTextIntact() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String id = place(session);
        JsonNode customer = receipt(id, "customer", session);
        assertThat(customer.get("title").asText()).isEqualTo("Hearth & Table · Customer Receipt");
        // Margherita 2000 + cheese 400 = 2400, x2 = 4800; STUDENT 10% = 480
        assertThat(lines(customer)).contains("Guest: " + NAME, "Note: " + NOTE, "Status: PLACED",
            "Discount (STUDENT): 480.00 KZT", "Total: 4320.00 KZT");
        JsonNode kitchen = receipt(id, "kitchen", session);
        assertThat(kitchen.get("title").asText()).isEqualTo("Hearth & Table · Kitchen Ticket");
        assertThat(lines(kitchen)).contains("Preparation note: " + NOTE, "Status: PLACED");
        assertThat(String.join("\n", lines(kitchen))).contains("Margherita").doesNotContain("KZT", "Total:", "4320.00", NAME);
    }
    @Test void defaultTypeIsCustomerAndTextAndHtmlFormatsAreUnchanged() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String id = place(session);
        String body = mvc.perform(get("/api/orders/" + id + "/receipt").session(session).param("format", "json"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(json.readTree(body).get("title").asText()).endsWith("Customer Receipt");
        mvc.perform(get("/api/orders/" + id + "/receipt").session(session).param("format", "text"))
            .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8"));
        mvc.perform(get("/api/orders/" + id + "/receipt").session(session))
            .andExpect(status().isOk()).andExpect(content().contentType("text/html;charset=UTF-8"));
    }
    @Test void unsupportedFormatsAndTypesAreRejected() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String id = place(session);
        for (String format : new String[]{"pdf", "xml", "JSON", "json ", "jsonp"})
            mvc.perform(get("/api/orders/" + id + "/receipt").session(session).param("format", format))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Receipt format must be text, html or json"));
        mvc.perform(get("/api/orders/" + id + "/receipt").session(session).param("format", "json").param("type", "manager"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Receipt type must be customer or kitchen"));
    }
    @Test void anotherSessionCannotReadTheJsonReceipt() throws Exception {
        String id = place(new MockHttpSession());
        for (String type : new String[]{"customer", "kitchen"}) {
            mvc.perform(get("/api/orders/" + id + "/receipt").session(new MockHttpSession()).param("format", "json").param("type", type))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Order not found in this session"));
            mvc.perform(get("/api/orders/" + id + "/receipt").param("format", "json").param("type", type))
                .andExpect(status().isNotFound());
        }
    }
}
