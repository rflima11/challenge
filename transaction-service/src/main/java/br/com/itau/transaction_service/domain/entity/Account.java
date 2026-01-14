package br.com.itau.transaction_service.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class Account implements Serializable {

    private UUID id;
    private BigDecimal balance;
    private AccountStatus status;

    public Account() {}

    public Account(UUID id, BigDecimal balance, AccountStatus status) {
        this.id = id;
        this.balance = balance;
        this.status = status;
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal");
        }
        this.balance = this.balance.subtract(amount);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
