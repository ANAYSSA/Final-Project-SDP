package com.restaurant.builder;

import com.restaurant.builder.application.OrderService;
import com.restaurant.builder.application.dto.*;
import com.restaurant.builder.product.Fulfillment;
import com.restaurant.builder.repository.OrderBook;
import com.restaurant.decorator.catalog.ToppingCatalog;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.strategy.context.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ConcurrentOrderTest {
    @Test void simultaneousRetriesCreateExactlyOneOrder() throws Exception {
        OrderService service = new OrderService(new MenuCatalog(), new ToppingCatalog(), new DiscountCatalog(), new PricingCalculator());
        OrderBook book = new OrderBook();
        OrderRequest request = new OrderRequest(List.of(new ItemRequest("pepperoni", 1, List.of())), "NONE", Fulfillment.TAKEAWAY,
            "Concurrent guest", null, "", UUID.randomUUID());
        try (var executor = Executors.newFixedThreadPool(8)) {
            List<Callable<UUID>> calls = IntStream.range(0, 32).mapToObj(i -> (Callable<UUID>) () -> service.place(request, book).id()).toList();
            var results = executor.invokeAll(calls);
            UUID expected = results.getFirst().get();
            for (var result : results) assertThat(result.get()).isEqualTo(expected);
        }
        assertThat(service.list(book)).hasSize(1);
        assertThat(service.list(book).getFirst().events()).hasSize(2);
    }
}
