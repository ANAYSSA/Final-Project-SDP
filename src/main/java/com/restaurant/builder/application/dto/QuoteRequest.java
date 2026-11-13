package com.restaurant.builder.application.dto;

import com.restaurant.builder.product.Fulfillment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record QuoteRequest(
    @NotNull @Size(min = 1, max = 30) List<@NotNull @Valid ItemRequest> items,
    @NotBlank @Size(max = 20) String discountCode,
    @NotNull Fulfillment fulfillment) {
    public QuoteRequest { if (items != null) items = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(items)); }
}
