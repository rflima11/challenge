package br.com.itau.transaction_service.infra.persistence.entity;

import br.com.itau.transaction_service.domain.entity.Account;
import br.com.itau.transaction_service.domain.entity.AccountStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_account_balance")
public class AccountBalanceEntity {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    public AccountBalanceEntity() {}

    public AccountBalanceEntity(UUID accountId, BigDecimal balance, AccountStatus status, LocalDateTime lastUpdatedAt) {
        this.accountId = accountId;
        this.balance = balance;
        this.status = status;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public static AccountBalanceEntity createNewAccount(Account account) {
        return new AccountBalanceEntity(
                account.getId(),
                account.getBalance(),
                account.getStatus(),
                LocalDateTime.now()
        );
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
