package com.creditcard.kafka;

import com.creditcard.kafka.aggregate.CreditCard;
import com.creditcard.kafka.repository.CardRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootApplication
@EnableScheduling
public class CreditCardApplication {

    private CardRepository repository;

    public CreditCardApplication(CardRepository repository) {
        this.repository = repository;
    }

    public static void main(String[] args) {
        SpringApplication.run(CreditCardApplication.class, args);
    }

    @Scheduled(fixedRate = 2000)
    public void randomCards() {
        CreditCard card = new CreditCard(UUID.randomUUID());
        card.assignLimit(new BigDecimal(2000));
        card.withdraw(BigDecimal.TEN.multiply(BigDecimal.TEN));
        card.repay(BigDecimal.TEN);
        repository.save(card);
    }
}