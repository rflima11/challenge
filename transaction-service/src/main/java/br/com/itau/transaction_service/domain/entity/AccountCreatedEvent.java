package br.com.itau.transaction_service.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountCreatedEvent(
        String accountId,
        BigDecimal initialBalance,
        LocalDateTime createdAt
) {}