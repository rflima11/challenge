package br.com.itau.transaction_service.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final Account account;
    private final TransactionType type;
    private final BigDecimal amount;
    private final LocalDateTime createdAt;
    private final String idempotencyKey;
    private TransactionStatus status;
    private BigDecimal balanceAfter;

    public Transaction(UUID id,
                       Account account,
                       TransactionType type,
                       BigDecimal amount,
                       LocalDateTime createdAt,
                       String idempotencyKey) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot create transaction for inactive account");
        }
        this.id = id;
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.createdAt = createdAt;
        this.idempotencyKey = idempotencyKey;
    }

    public void conclude(BigDecimal currentBalance) {
        this.balanceAfter = currentBalance;
        this.status = TransactionStatus.SUCCESS;
    }

    public UUID getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Account getAccount() {
        return account;
    }
}
