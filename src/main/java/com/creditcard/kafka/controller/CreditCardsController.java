package com.creditcard.kafka.controller;

import com.creditcard.kafka.aggregate.CreditCard;
import com.creditcard.kafka.dto.CardRequest;
import com.creditcard.kafka.dto.TransactionType;
import com.creditcard.kafka.repository.CardRepository;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore;

@RestController
class CreditCardsController {

    @Value("${kafka.credit-card.snapshot.topic}")
    private String SNAPSHOTS_FOR_CARDS_TOPIC;

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    private final CardRepository repository;

    CreditCardsController(StreamsBuilderFactoryBean kStreamBuilderFactoryBean, CardRepository repository) {
        this.streamsBuilderFactoryBean = kStreamBuilderFactoryBean;
        this.repository = repository;
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CreditCard>> creditCardList() {

        List<CreditCard> cards = new ArrayList<>();

        fetchCreditCardsSnapShot()
                .all().forEachRemaining(
                stringCreditCardKeyValue -> cards.add(stringCreditCardKeyValue.value)
        );

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/cards/{uuid}")
    public ResponseEntity<CreditCard> getCard(@PathVariable("uuid") UUID uuid) {

        CreditCard creditCard = fetchCreditCardsSnapShot().
                get(uuid.toString());

        return ResponseEntity.ok(creditCard);
    }

    @PostMapping("/cards")
    public ResponseEntity<String> moneyTransaction(@RequestBody CardRequest request, @RequestParam TransactionType type) {

        CreditCard creditCard = fetchCreditCardsSnapShot().
                get(request.getUuid().toString());

        if(type == TransactionType.WITHDRAW) {
            creditCard.withdraw(request.getAmount());
            repository.save(creditCard);
        }else if(type == TransactionType.REPAY){
            creditCard.repay(request.getAmount());
            repository.save(creditCard);
        }

        return ResponseEntity.ok("done");
    }

    private ReadOnlyKeyValueStore<String, CreditCard> fetchCreditCardsSnapShot() {

        return streamsBuilderFactoryBean
                .getKafkaStreams()
                .store(SNAPSHOTS_FOR_CARDS_TOPIC, keyValueStore());
    }
}