package com.restaurant.builder;

import com.restaurant.builder.product.*;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.strategy.concrete.PickupDiscount;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class OrderBuilderTest {
    private OrderLine line() { return new OrderLine(new MenuCatalog().create("margherita"), 2, List.of()); }
    @Test void productSnapshotsBuilderState() {
        Order.Builder builder = new Order.Builder().customer("  Anuar  ").fulfillment(Fulfillment.TAKEAWAY).addItem(line());
        Order first = builder.build();
        builder.addItem(line()).discount(new PickupDiscount());
        Order second = builder.build();
        assertThat(first.customerName()).isEqualTo("Anuar");
        assertThat(first.items()).hasSize(1);
        assertThat(first.pricing().total()).isEqualByComparingTo("4000");
        assertThat(second.pricing().total()).isEqualByComparingTo("6400");
        assertThatThrownBy(() -> first.items().add(line())).isInstanceOf(UnsupportedOperationException.class);
    }
    @Test void builderRejectsIncompleteOrInconsistentOrders() {
        assertThatThrownBy(() -> new Order.Builder().build()).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Order.Builder().customer("Anuar").fulfillment(Fulfillment.DINE_IN).addItem(line()).build())
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("table");
        assertThatThrownBy(() -> new Order.Builder().customer("Anuar").fulfillment(Fulfillment.TAKEAWAY).tableNumber(2).addItem(line()).build())
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Order.Builder().customer("Anuar").fulfillment(Fulfillment.TAKEAWAY).build())
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("1 to 30");
    }
    @Test void quantitiesCannotBeNegativeOrOverflowTotals() {
        for (int invalid : new int[]{Integer.MIN_VALUE, -1, 0, 11, Integer.MAX_VALUE}) {
            assertThatThrownBy(() -> new OrderLine(new MenuCatalog().create("margherita"), invalid, List.of())).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
