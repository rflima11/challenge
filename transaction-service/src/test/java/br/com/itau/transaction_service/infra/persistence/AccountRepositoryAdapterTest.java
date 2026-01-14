package br.com.itau.transaction_service.infra.persistence;

import br.com.itau.transaction_service.domain.entity.Account;
import br.com.itau.transaction_service.domain.entity.AccountStatus;
import br.com.itau.transaction_service.infra.persistence.entity.AccountBalanceEntity;
import br.com.itau.transaction_service.infra.persistence.repository.JpaAccountBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryAdapterTest {

    @Autowired
    private JpaAccountBalanceRepository jpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AccountRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        this.adapter = new AccountRepositoryAdapter(jpaRepository);
    }

    @Test
    @DisplayName("Should create a new account balance in the database")
    void shouldCreateBalance() {
        UUID accountId = UUID.randomUUID();
        Account domainAccount = new Account(accountId, new BigDecimal("100.00"), AccountStatus.ACTIVE);

        adapter.createBalance(domainAccount);

        AccountBalanceEntity savedEntity = entityManager.find(AccountBalanceEntity.class, accountId);

        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getAccountId()).isEqualTo(accountId);
        assertThat(savedEntity.getBalance()).isEqualByComparingTo("100.00");
        assertThat(savedEntity.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should load account by ID mapping Entity to Domain correctly")
    void shouldLoadById() {
        UUID accountId = UUID.randomUUID();
        AccountBalanceEntity entity = new AccountBalanceEntity();
        entity.setAccountId(accountId);
        entity.setBalance(new BigDecimal("50.50"));
        entity.setStatus(AccountStatus.ACTIVE);

        entityManager.persistAndFlush(entity);

        Optional<Account> result = adapter.loadById(accountId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(accountId);
        assertThat(result.get().getBalance()).isEqualByComparingTo("50.50");
        assertThat(result.get().getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return empty when loading non-existent ID")
    void shouldReturnEmptyWhenNotFound() {
        Optional<Account> result = adapter.loadById(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should update existing account balance")
    void shouldUpdateBalance() {
        UUID accountId = UUID.randomUUID();
        AccountBalanceEntity entity = new AccountBalanceEntity();
        entity.setAccountId(accountId);
        entity.setBalance(new BigDecimal("10.00"));
        entity.setStatus(AccountStatus.ACTIVE);
        entityManager.persistAndFlush(entity);

        Account accountToUpdate = new Account(accountId, new BigDecimal("200.00"), AccountStatus.ACTIVE);

        adapter.updateBalance(accountToUpdate);

        entityManager.flush();
        entityManager.clear();

        AccountBalanceEntity updatedEntity = entityManager.find(AccountBalanceEntity.class, accountId);

        assertThat(updatedEntity.getBalance()).isEqualByComparingTo("200.00");
    }
}