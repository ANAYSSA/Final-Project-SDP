package com.restaurant.strategy;

import com.restaurant.builder.product.*;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.strategy.concrete.*;
import com.restaurant.strategy.context.PricingCalculator;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PricingTest {
    private final PricingCalculator pricing = new PricingCalculator();
    private final List<OrderLine> lines = List.of(new OrderLine(new MenuCatalog().create("pepperoni"), 2, List.of()));
    @Test void contextCanSwapStrategiesWithoutChangingOrderLines() {
        assertThat(pricing.calculate(lines, Fulfillment.TAKEAWAY, new NoDiscount()).total()).isEqualByComparingTo("5000");
        assertThat(pricing.calculate(lines, Fulfillment.TAKEAWAY, new StudentDiscount()).total()).isEqualByComparingTo("4500");
        assertThat(pricing.calculate(lines, Fulfillment.TAKEAWAY, new PickupDiscount()).total()).isEqualByComparingTo("4000");
    }
    @Test void pickupCannotApplyToDineIn() {
        assertThatThrownBy(() -> pricing.calculate(lines, Fulfillment.DINE_IN, new PickupDiscount())).isInstanceOf(IllegalArgumentException.class);
    }
}
