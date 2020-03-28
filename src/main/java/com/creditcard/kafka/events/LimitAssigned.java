package com.creditcard.kafka.events;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class LimitAssigned implements DomainEvent {

    private UUID uuid;
    private Date date;
    private BigDecimal limit;
    private String type = "card.assigned";

    public LimitAssigned(UUID uuid, BigDecimal limit,Date date) {
        this.uuid = uuid;
        this.date = date;
        this.limit = limit;
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
