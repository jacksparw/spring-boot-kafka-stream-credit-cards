package com.creditcard.kafka.events;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CardRepaid implements DomainEvent {
    private UUID uuid;
    private BigDecimal amount;
    private Date date;
    private String type = "card.repaid";


    public CardRepaid(UUID uuid, BigDecimal amount, Date date) {
        this.uuid = uuid;
        this.amount = amount;
        this.date = date;
    }

    @Override
    public UUID aggregateUUID() {
        return uuid;
    }

    @Override
    public Date timestamp() {
        return date;
    }
}
