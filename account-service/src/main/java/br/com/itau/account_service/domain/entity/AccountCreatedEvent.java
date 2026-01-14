package br.com.itau.account_service.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountCreatedEvent(
        String accountId,
        BigDecimal initialBalance,
        LocalDateTime createdAt
) {}
