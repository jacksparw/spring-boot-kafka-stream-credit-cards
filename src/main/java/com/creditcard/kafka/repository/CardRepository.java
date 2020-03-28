package com.creditcard.kafka.repository;

import com.creditcard.kafka.aggregate.CreditCard;
import com.creditcard.kafka.events.DomainEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Log4j2
public class CardRepository {

    public static final String S1P_CREDIT_CARDS_EVENTS = "credit-cards-input-event";
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    public CardRepository(KafkaTemplate<String, DomainEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void save(CreditCard creditCard) {

        creditCard.getDirtyEvents()
                .forEach(domainEvent -> sendMessage(domainEvent, kafkaTemplate));

        creditCard.eventsFlushed();
    }

    private static ListenableFuture<SendResult<String, DomainEvent>> sendMessage(DomainEvent domainEvent, KafkaTemplate<String, DomainEvent> kafkaTemplate) {

        ListenableFuture<SendResult<String, DomainEvent>> future = kafkaTemplate.send(S1P_CREDIT_CARDS_EVENTS,domainEvent.aggregateUUID().toString(), domainEvent);

        future.addCallback(new ListenableFutureCallback<SendResult<String, DomainEvent>>() {
            @Override
            public void onSuccess(SendResult<String, DomainEvent> result) {
                log.debug("On Success : event sent to kafka");
            }

            @Override
            public void onFailure(Throwable ex) {
                log.info("On Failure: error while sending event to kafka"+ex.getMessage());
            }
        });

        return future;
    }
}
