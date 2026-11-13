package com.restaurant.bridge;

import com.restaurant.bridge.abstraction.*;
import com.restaurant.bridge.implementation.*;
import com.restaurant.builder.product.*;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.observer.event.OrderStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ReceiptBridgeTest {
    private Order order() {
        return new Order.Builder().customer("<script>alert('x')</script>").fulfillment(Fulfillment.TAKEAWAY)
            .note("<img src=x onerror=alert(1)>").addItem(new OrderLine(new MenuCatalog().create("pesto"), 1, List.of())).build();
    }
    @Test void bothDocumentTypesWorkWithBothIndependentRenderers() {
        for (ReceiptRenderer renderer : List.of(new TextReceiptRenderer(), new HtmlReceiptRenderer())) {
            String customer = new CustomerReceipt(renderer).render(order(), OrderStatus.READY);
            String kitchen = new KitchenTicket(renderer).render(order(), OrderStatus.READY);
            assertThat(customer).contains("Customer Receipt", "Total: 2600.00 KZT", "Basil Pesto");
            assertThat(kitchen).contains("Kitchen Ticket", "Preparation note", "Basil Pesto").doesNotContain("2600.00", "Total:");
        }
    }
    @Test void htmlRendererEscapesAllUntrustedContent() {
        String html = new CustomerReceipt(new HtmlReceiptRenderer()).render(order(), OrderStatus.PLACED);
        assertThat(html).contains("&lt;script&gt;", "&lt;img", "&#39;").doesNotContain("<script>", "<img ");
    }
}
