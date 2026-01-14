package br.com.itau.transaction_service.application.port.in;

import br.com.itau.transaction_service.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionCommand(
        UUID accountId,
        BigDecimal amount,
        TransactionType type,
        String idempotencyKey
) {}
