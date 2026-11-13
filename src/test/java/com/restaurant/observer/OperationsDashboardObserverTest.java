package com.restaurant.observer;

import com.restaurant.observer.concrete.CustomerObserver;
import com.restaurant.observer.concrete.KitchenObserver;
import com.restaurant.observer.concrete.OperationsDashboardObserver;
import com.restaurant.observer.event.OrderNotification;
import com.restaurant.observer.event.OrderStatus;
import com.restaurant.observer.event.StatusChange;
import com.restaurant.observer.subject.OrderProgress;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class OperationsDashboardObserverTest {
    private static final UUID ORDER_ID = UUID.fromString("abcdef12-3456-7890-abcd-ef1234567890");

    @Test void notificationContainsOnlyPublicReferenceStatusAndAggregateQuantity() {
        Instant occurredAt = Instant.parse("2026-11-13T12:00:00Z");
        var observer = new OperationsDashboardObserver(5);

        OrderNotification notification = observer.onStatusChanged(new StatusChange(ORDER_ID, OrderStatus.READY, occurredAt));

        assertThat(notification).isEqualTo(new OrderNotification(OrderStatus.READY,
            "Order ABCDEF12 | READY | Items: 5", "OPERATIONS", occurredAt));
        assertThat(notification.message()).doesNotContain(ORDER_ID.toString());
    }

    @Test void everyPublishedStatusReachesEachSubscriberOnceEvenAfterDuplicateSubscription() {
        OrderProgress subject = new OrderProgress(ORDER_ID);
        var operations = new OperationsDashboardObserver(5);
        subject.subscribe(new CustomerObserver());
        subject.subscribe(new KitchenObserver());
        subject.subscribe(operations);
        subject.subscribe(operations);

        subject.announce();
        int publishedStatuses = 0;
        for (OrderStatus status : OrderStatus.values()) {
            if (status != OrderStatus.PLACED) subject.advance();
            publishedStatuses++;
            assertThat(subject.notifications()).hasSize(publishedStatuses * 3);
            for (String audience : List.of("CUSTOMER", "KITCHEN", "OPERATIONS")) {
                assertThat(subject.notifications()).filteredOn(event -> event.audience().equals(audience) && event.status() == status)
                    .hasSize(1);
            }
            List<OrderNotification> transition = subject.notifications().stream().filter(event -> event.status() == status).toList();
            assertThat(transition).extracting(OrderNotification::occurredAt).containsOnly(transition.getFirst().occurredAt());
            assertThat(transition).filteredOn(event -> event.audience().equals("OPERATIONS"))
                .extracting(OrderNotification::message).containsExactly("Order ABCDEF12 | " + status + " | Items: 5");
        }
        assertThat(operationsEvents(subject)).extracting(OrderNotification::status).containsExactly(OrderStatus.values());
    }

    @Test void rejectedTransitionsNeverPublishNewNotifications() {
        OrderProgress subject = new OrderProgress(ORDER_ID);
        subject.subscribe(new OperationsDashboardObserver(2));

        assertThatThrownBy(subject::advance).isInstanceOf(IllegalStateException.class);
        assertThat(subject.notifications()).isEmpty();
        assertThat(subject.status()).isEqualTo(OrderStatus.PLACED);

        subject.announce();
        List<OrderNotification> placed = subject.notifications();
        assertThatThrownBy(subject::announce).isInstanceOf(IllegalStateException.class);
        assertThat(subject.notifications()).isEqualTo(placed);

        subject.advance();
        subject.advance();
        subject.advance();
        List<OrderNotification> completed = subject.notifications();
        assertThat(completed).hasSize(4);
        assertThatThrownBy(subject::advance).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(subject::announce).isInstanceOf(IllegalStateException.class);
        assertThat(subject.notifications()).isEqualTo(completed);
        assertThat(subject.status()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test void unsubscribingOperationsLeavesKitchenAndCustomerSubscribed() {
        OrderProgress subject = new OrderProgress(ORDER_ID);
        var operations = new OperationsDashboardObserver(3);
        subject.subscribe(new CustomerObserver());
        subject.subscribe(new KitchenObserver());
        subject.subscribe(operations);
        subject.announce();
        subject.advance();
        List<OrderNotification> beforeUnsubscribe = operationsEvents(subject);

        subject.unsubscribe(operations);
        subject.advance();
        subject.advance();

        assertThat(operationsEvents(subject)).isEqualTo(beforeUnsubscribe).hasSize(2);
        for (String audience : List.of("CUSTOMER", "KITCHEN")) {
            assertThat(subject.notifications()).filteredOn(event -> event.audience().equals(audience))
                .extracting(OrderNotification::status).containsExactly(OrderStatus.values());
        }
        assertThat(subject.notifications()).hasSize(10);
    }

    @ParameterizedTest @ValueSource(ints = {Integer.MIN_VALUE, -1, 0})
    void aggregateQuantityMustBePositive(int quantity) {
        assertThatThrownBy(() -> new OperationsDashboardObserver(quantity)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Item quantity must be positive");
    }

    private List<OrderNotification> operationsEvents(OrderProgress subject) {
        return subject.notifications().stream().filter(event -> event.audience().equals("OPERATIONS")).toList();
    }
}
