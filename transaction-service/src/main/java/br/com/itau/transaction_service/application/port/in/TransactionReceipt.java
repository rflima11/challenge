package br.com.itau.transaction_service.application.port.in;

import br.com.itau.transaction_service.domain.entity.Transaction;
import br.com.itau.transaction_service.domain.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionReceipt(
        UUID transactionId,
        TransactionStatus status,
        BigDecimal amount,
        String type,
        BigDecimal currentBalance,
        LocalDateTime createdAt,
        String idempotencyKey
) {
    public static TransactionReceipt from(Transaction transaction) {
        return new TransactionReceipt(
                transaction.getId(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getType().name(),
                transaction.getBalanceAfter(),
                transaction.getCreatedAt(),
                transaction.getIdempotencyKey()
        );
    }
}
