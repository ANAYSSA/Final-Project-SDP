package com.restaurant.builder.web;

import com.restaurant.builder.application.OrderService;
import com.restaurant.builder.application.dto.*;
import com.restaurant.builder.repository.OrderBook;
import com.restaurant.strategy.context.Pricing;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public final class OrderController {
    private static final String BOOK = OrderBook.class.getName();
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }
    @GetMapping("/health") public Map<String, String> health() { return Map.of("status", "UP"); }
    @GetMapping("/menu") public MenuView menu() { return service.menu(); }
    @PostMapping(value = "/orders/quote", consumes = MediaType.APPLICATION_JSON_VALUE) public Pricing quote(@Valid @RequestBody QuoteRequest request) { return service.quote(request); }
    @PostMapping(value = "/orders", consumes = MediaType.APPLICATION_JSON_VALUE) public ResponseEntity<OrderView> place(@Valid @RequestBody OrderRequest request, HttpSession session) {
        return ResponseEntity.status(201).body(service.place(request, book(session)));
    }
    @GetMapping("/orders") public List<OrderView> list(HttpSession session) { return service.list(book(session)); }
    @GetMapping("/orders/{id}") public OrderView get(@PathVariable UUID id, HttpSession session) { return service.get(id, book(session)); }
    @PostMapping(value = "/orders/{id}/advance", consumes = MediaType.APPLICATION_JSON_VALUE) public OrderView advance(@PathVariable UUID id, HttpSession session) { return service.advance(id, book(session)); }
    @GetMapping("/orders/{id}/receipt") public ResponseEntity<String> receipt(@PathVariable UUID id,
            @RequestParam(defaultValue = "html") String format, @RequestParam(defaultValue = "customer") String type, HttpSession session) {
        OrderService.RenderedReceipt receipt = service.receipt(id, format, type, book(session));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(receipt.contentType()))
            .header("Cache-Control", "no-store").header("X-Content-Type-Options", "nosniff")
            .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; frame-ancestors 'none'")
            .body(receipt.body());
    }
    private OrderBook book(HttpSession session) {
        synchronized (session) {
            OrderBook book = (OrderBook) session.getAttribute(BOOK);
            if (book == null) {
                book = new OrderBook();
                session.setAttribute(BOOK, book);
            }
            return book;
        }
    }
}
