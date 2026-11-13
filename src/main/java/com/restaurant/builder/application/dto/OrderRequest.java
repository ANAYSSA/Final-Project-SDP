package com.restaurant.builder.application.dto;

import com.restaurant.builder.product.Fulfillment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record OrderRequest(
    @NotNull @Size(min = 1, max = 30) List<@NotNull @Valid ItemRequest> items,
    @NotBlank @Size(max = 20) String discountCode,
    @NotNull Fulfillment fulfillment,
    @NotBlank @Size(min = 2, max = 60) String customerName,
    @Min(1) @Max(30) Integer tableNumber,
    @Size(max = 300) String note,
    @NotNull UUID requestId) {
    public OrderRequest {
        if (items != null) items = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(items));
        if (note == null) note = "";
    }
    public QuoteRequest quoteRequest() { return new QuoteRequest(items, discountCode, fulfillment); }
}
