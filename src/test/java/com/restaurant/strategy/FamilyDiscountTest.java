package com.restaurant.strategy;

import com.restaurant.builder.product.*;
import com.restaurant.decorator.catalog.ToppingCatalog;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.factory.product.Meal;
import com.restaurant.factory.product.MealCategory;
import com.restaurant.strategy.api.DiscountStrategy;
import com.restaurant.strategy.concrete.*;
import com.restaurant.strategy.context.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.assertj.core.api.Assertions.*;

class FamilyDiscountTest {
    private final PricingCalculator pricing = new PricingCalculator();
    private final DiscountCatalog catalog = new DiscountCatalog();
    private final MenuCatalog menu = new MenuCatalog();
    private final ToppingCatalog toppings = new ToppingCatalog();

    /** Test-only meal with an arbitrary price, so exact boundary subtotals such as 5999 can be expressed. */
    private record FixedPriceMeal(BigDecimal price) implements Meal {
        public String id() { return "fixed"; }
        public String name() { return "Fixed price meal"; }
        public String description() { return "Boundary test meal"; }
        public MealCategory category() { return MealCategory.PASTA; }
    }
    private List<OrderLine> subtotal(String amount) { return List.of(new OrderLine(new FixedPriceMeal(new BigDecimal(amount)), 1, List.of())); }
    private Pricing family(List<OrderLine> lines, Fulfillment fulfillment) { return pricing.calculate(lines, fulfillment, catalog.find("FAMILY")); }

    @ParameterizedTest @EnumSource(Fulfillment.class)
    void subtotalBelowThresholdIsRejectedWithAUsefulMessage(Fulfillment fulfillment) {
        assertThatThrownBy(() -> family(subtotal("5999"), fulfillment))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least 6000.00 KZT").hasMessageContaining("5999.00");
        assertThatThrownBy(() -> family(subtotal("5999.99"), fulfillment)).isInstanceOf(IllegalArgumentException.class);
    }
    @ParameterizedTest @EnumSource(Fulfillment.class)
    void exactlyThresholdGivesFifteenPercentForBothFulfillments(Fulfillment fulfillment) {
        Pricing result = family(subtotal("6000"), fulfillment);
        assertThat(result.subtotal()).isEqualByComparingTo("6000.00");
        assertThat(result.discount()).isEqualByComparingTo("900.00");
        assertThat(result.total()).isEqualByComparingTo("5100.00");
    }
    @Test void largeOrderGetsFifteenPercent() {
        Pricing result = family(subtotal("10000"), Fulfillment.TAKEAWAY);
        assertThat(result.discount()).isEqualByComparingTo("1500.00");
        assertThat(result.total()).isEqualByComparingTo("8500.00");
    }
    @Test void discountUsesProjectScaleTwoHalfUpRounding() {
        // 6000.30 x 0.15 = 900.045 -> HALF_UP gives 900.05 (HALF_EVEN would give 900.04)
        Pricing result = family(subtotal("6000.30"), Fulfillment.DINE_IN);
        assertThat(result.discount()).isEqualTo(new BigDecimal("900.05"));
        assertThat(result.total()).isEqualTo(new BigDecimal("5100.25"));
    }
    @Test void toppingsAndQuantitiesCountTowardsTheThresholdBeforeDiscount() {
        Meal pesto = menu.create("pesto");                                    // 2600
        Meal pestoWithCheese = toppings.add(menu.create("pesto"), "cheese");  // 2600 + 400 = 3000
        assertThatThrownBy(() -> family(List.of(new OrderLine(pesto, 2, List.of())), Fulfillment.TAKEAWAY))
            .hasMessageContaining("5200.00");
        assertThatThrownBy(() -> family(List.of(new OrderLine(pestoWithCheese, 1, List.of("Extra cheese"))), Fulfillment.TAKEAWAY))
            .hasMessageContaining("3000.00");
        Pricing result = family(List.of(new OrderLine(pestoWithCheese, 2, List.of("Extra cheese"))), Fulfillment.TAKEAWAY);
        assertThat(result.subtotal()).isEqualByComparingTo("6000.00");
        assertThat(result.discount()).isEqualByComparingTo("900.00");
        assertThat(result.total()).isEqualByComparingTo("5100.00");
    }
    @Test void registryExposesEveryStrategyOnceUnderItsOwnCode() {
        assertThat(catalog.items()).extracting(DiscountCatalog.DiscountOption::id).containsExactly("NONE", "STUDENT", "PICKUP", "FAMILY");
        for (DiscountCatalog.DiscountOption option : catalog.items()) {
            DiscountStrategy strategy = catalog.find(option.id());
            assertThat(strategy.code()).isEqualTo(option.id());
        }
        assertThat(catalog.find("FAMILY")).isInstanceOf(FamilyDiscount.class);
    }
    @Test void familyCannotBeStackedWithOtherDiscounts() {
        for (String combined : new String[]{"FAMILY,STUDENT", "FAMILY+PICKUP", "STUDENT FAMILY", "family"})
            assertThatThrownBy(() -> catalog.find(combined)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown discount code");
    }
    @Test void existingStrategiesKeepTheirBehaviour() {
        List<OrderLine> lines = subtotal("6000");
        assertThat(pricing.calculate(lines, Fulfillment.DINE_IN, catalog.find("NONE")).total()).isEqualByComparingTo("6000.00");
        assertThat(pricing.calculate(lines, Fulfillment.DINE_IN, catalog.find("STUDENT")).total()).isEqualByComparingTo("5400.00");
        assertThat(pricing.calculate(lines, Fulfillment.TAKEAWAY, catalog.find("PICKUP")).total()).isEqualByComparingTo("4800.00");
        assertThatThrownBy(() -> pricing.calculate(lines, Fulfillment.DINE_IN, catalog.find("PICKUP"))).isInstanceOf(IllegalArgumentException.class);
    }
}
