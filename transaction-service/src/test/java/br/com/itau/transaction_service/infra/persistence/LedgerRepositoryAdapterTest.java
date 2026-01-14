package br.com.itau.transaction_service.infra.persistence;

import br.com.itau.transaction_service.application.port.in.TransactionReceipt;
import br.com.itau.transaction_service.domain.entity.Account;
import br.com.itau.transaction_service.domain.entity.AccountStatus;
import br.com.itau.transaction_service.domain.entity.Transaction;
import br.com.itau.transaction_service.domain.entity.TransactionType;
import br.com.itau.transaction_service.infra.persistence.entity.AccountBalanceEntity;
import br.com.itau.transaction_service.infra.persistence.entity.TransactionLedgerEntity;
import br.com.itau.transaction_service.infra.persistence.repository.JpaLedgerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class LedgerRepositoryAdapterTest {

    @Autowired
    private JpaLedgerRepository jpaLedgerRepository;

    @Autowired
    private TestEntityManager entityManager;

    private LedgerRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        this.adapter = new LedgerRepositoryAdapter(jpaLedgerRepository);
    }

    @Test
    @DisplayName("Should save transaction successfully mapping domain to entity fields correctly")
    void shouldSaveTransaction() {
        UUID accountId = UUID.randomUUID();

        AccountBalanceEntity accountEntity = new AccountBalanceEntity();
        accountEntity.setAccountId(accountId);
        accountEntity.setBalance(new BigDecimal("150.00"));
        accountEntity.setStatus(AccountStatus.ACTIVE);
        entityManager.persistAndFlush(accountEntity);

        Account domainAccount = new Account(accountId, new BigDecimal("150.00"), AccountStatus.ACTIVE);
        UUID domainTransactionId = UUID.randomUUID();
        String idempotencyKey = "key-123";

        Transaction transaction = new Transaction(
                domainTransactionId,
                domainAccount,
                TransactionType.DEPOSIT,
                new BigDecimal("50.00"),
                LocalDateTime.now(),
                idempotencyKey
        );
        transaction.conclude(new BigDecimal("150.00"));

        adapter.save(transaction);

        entityManager.flush();
        entityManager.clear();

        TransactionLedgerEntity savedEntity = entityManager.getEntityManager()
                .createQuery("SELECT t FROM TransactionLedgerEntity t WHERE t.idempotencyKey = :key", TransactionLedgerEntity.class)
                .setParameter("key", idempotencyKey)
                .getSingleResult();

        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getAccountId()).isEqualTo(accountId);
        assertThat(savedEntity.getAmount()).isEqualByComparingTo("50.00");
        assertThat(savedEntity.getBalanceAfter()).isEqualByComparingTo("150.00");
        assertThat(savedEntity.getOperation()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(savedEntity.getIdempotencyKey()).isEqualTo(idempotencyKey);
    }

    @Test
    @DisplayName("Should find transaction receipt by idempotency key")
    void shouldFindByIdempotencyKey() {
        UUID accountId = UUID.randomUUID();

        AccountBalanceEntity accountEntity = new AccountBalanceEntity();
        accountEntity.setAccountId(accountId);
        accountEntity.setBalance(new BigDecimal("100.00"));
        accountEntity.setStatus(AccountStatus.ACTIVE);
        entityManager.persistAndFlush(accountEntity);

        String idempotencyKey = "key-existing";

        TransactionLedgerEntity ledgerEntity = new TransactionLedgerEntity();
        ledgerEntity.setAccountId(accountId);
        ledgerEntity.setAmount(new BigDecimal("20.00"));
        ledgerEntity.setBalanceAfter(new BigDecimal("80.00"));
        ledgerEntity.setOperation(TransactionType.WITHDRAWAL);
        ledgerEntity.setIdempotencyKey(idempotencyKey);

        entityManager.persistAndFlush(ledgerEntity);

        Optional<TransactionReceipt> result = adapter.findByIdempotencyKey(idempotencyKey);

        assertThat(result).isPresent();
        assertThat(result.get().idempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(result.get().amount()).isEqualByComparingTo("20.00");
        assertThat(result.get().currentBalance()).isEqualByComparingTo("80.00");
        assertThat(result.get().type()).isEqualTo("WITHDRAWAL");
    }

    @Test
    @DisplayName("Should return empty when searching for non-existent idempotency key")
    void shouldReturnEmptyWhenKeyNotFound() {
        Optional<TransactionReceipt> result = adapter.findByIdempotencyKey("non-existent-key");
        assertThat(result).isEmpty();
    }
}