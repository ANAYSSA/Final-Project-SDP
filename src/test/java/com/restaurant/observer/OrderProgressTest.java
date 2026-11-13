package com.restaurant.observer;

import com.restaurant.observer.concrete.*;
import com.restaurant.observer.event.OrderStatus;
import com.restaurant.observer.subject.OrderProgress;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class OrderProgressTest {
    @Test void subscribersReceiveEachTransitionAndCanUnsubscribe() {
        OrderProgress subject = new OrderProgress(UUID.randomUUID());
        CustomerObserver customer = new CustomerObserver();
        KitchenObserver kitchen = new KitchenObserver();
        subject.subscribe(customer);
        subject.subscribe(kitchen);
        subject.subscribe(kitchen);
        subject.announce();
        assertThat(subject.notifications()).hasSize(2);
        subject.advance();
        assertThat(subject.notifications()).hasSize(4);
        subject.unsubscribe(kitchen);
        subject.advance();
        assertThat(subject.notifications()).hasSize(5);
        assertThat(subject.notifications().getLast().audience()).isEqualTo("CUSTOMER");
        assertThat(subject.status()).isEqualTo(OrderStatus.READY);
        subject.advance();
        assertThat(subject.status()).isEqualTo(OrderStatus.COMPLETED);
        assertThatThrownBy(subject::advance).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(subject::announce).isInstanceOf(IllegalStateException.class);
        assertThat(subject.notifications()).hasSize(6);
    }
}
