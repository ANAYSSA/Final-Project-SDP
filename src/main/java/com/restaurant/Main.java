package com.restaurant;

import com.restaurant.builder.application.*;
import com.restaurant.decorator.catalog.ToppingCatalog;
import com.restaurant.factory.catalog.MenuCatalog;
import com.restaurant.strategy.context.*;
import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** The only top-level class outside the six pattern packages. */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--demo")) { PatternDemo.run(); return; }
        SpringApplication.run(Main.class, args);
    }
    @Bean OrderService orderService() {
        return new OrderService(new MenuCatalog(), new ToppingCatalog(), new DiscountCatalog(), new PricingCalculator());
    }
}
