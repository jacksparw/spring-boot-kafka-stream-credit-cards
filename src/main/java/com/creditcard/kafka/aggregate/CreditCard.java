package com.creditcard.kafka.aggregate;

import com.creditcard.kafka.events.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javaslang.API;
import javaslang.Predicates;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.*;

import static javaslang.API.Case;

@NoArgsConstructor
@ToString
@Getter
public class CreditCard {

    private UUID uuid;
    private BigDecimal limit;
    private BigDecimal used = BigDecimal.ZERO;

    public List<DomainEvent> getDirtyEvents() {
        return Collections.unmodifiableList(dirtyEvents);
    }

    private @JsonIgnore final List<DomainEvent> dirtyEvents = new ArrayList<>();

    public CreditCard(UUID uuid) {
        cardCreated(new CardCreated(uuid, new Date()));
    }

    private CreditCard cardCreated(CardCreated cardCreated){
        this.uuid = cardCreated.getUuid();
        dirtyEvents.add(cardCreated);
        return this;
    }

    public void assignLimit(BigDecimal amount) {
        if(limitAlreadyAssigned()) {
            throw new IllegalStateException();
        }
        limitAssigned(new LimitAssigned(uuid, amount, new Date()));
    }

    private CreditCard limitAssigned(LimitAssigned limitAssigned) {
        this.limit = limitAssigned.getLimit();
        dirtyEvents.add(limitAssigned);
        return this;
    }

    private boolean limitAlreadyAssigned() {
        return limit != null;
    }

    public void withdraw(BigDecimal amount) {
        if(notEnoughMoneyToWithdraw(amount)) {
            throw new IllegalStateException();
        }
        cardWithdrawn(new CardWithdrawn(uuid, amount, new Date()));
    }

    private CreditCard cardWithdrawn(CardWithdrawn cardWithdrawn) {
        this.used = used.add(cardWithdrawn.getAmount());
        dirtyEvents.add(cardWithdrawn);
        return this;

    }

    private boolean notEnoughMoneyToWithdraw(BigDecimal amount) {
        return availableLimit().compareTo(amount) < 0;
    }

    public void repay(BigDecimal amount) {
        cardRepaid(new CardRepaid(uuid, amount, new Date()));
    }

    private CreditCard cardRepaid(CardRepaid cardRepaid) {
        used = used.subtract(cardRepaid.getAmount());
        dirtyEvents.add(cardRepaid);
        return this;
    }

    public BigDecimal availableLimit() {
        return limit.subtract(used);
    }

    public void eventsFlushed() {
        dirtyEvents.clear();
    }

    public CreditCard handle(DomainEvent domainEvent) {

        CreditCard creditCard = API.Match(domainEvent).of(
                Case(Predicates.instanceOf(CardCreated.class), this::cardCreated),
                Case(Predicates.instanceOf(LimitAssigned.class), this::limitAssigned),
                Case(Predicates.instanceOf(CardRepaid.class), this::cardRepaid),
                Case(Predicates.instanceOf(CardWithdrawn.class), this::cardWithdrawn)
        );

        creditCard.eventsFlushed();

        return creditCard;
    }
}
