package br.com.itau.transaction_service.infra.persistence.entity;

import br.com.itau.transaction_service.application.port.in.TransactionReceipt;
import br.com.itau.transaction_service.domain.entity.Transaction;
import br.com.itau.transaction_service.domain.entity.TransactionStatus;
import br.com.itau.transaction_service.domain.entity.TransactionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_transaction_ledger")
public class TransactionLedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType operation;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static TransactionLedgerEntity fromTransaction(Transaction transaction) {
        TransactionLedgerEntity entity = new TransactionLedgerEntity();
        entity.setAccountId(transaction.getAccount().getId());
        entity.setOperation(transaction.getType());
        entity.setAmount(transaction.getAmount());
        entity.setBalanceAfter(transaction.getBalanceAfter());
        entity.setIdempotencyKey(transaction.getIdempotencyKey());
        return entity;
    }

    public TransactionReceipt toTransactionReceipt() {
        return new TransactionReceipt(
                id,
                TransactionStatus.SUCCESS,
                amount,
                operation.name(),
                balanceAfter,
                createdAt,
                idempotencyKey
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public TransactionType getOperation() {
        return operation;
    }

    public void setOperation(TransactionType operation) {
        this.operation = operation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
