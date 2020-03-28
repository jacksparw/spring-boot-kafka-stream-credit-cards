package com.creditcard.kafka.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CardRequest {
    private UUID uuid;
    private BigDecimal amount;
}
