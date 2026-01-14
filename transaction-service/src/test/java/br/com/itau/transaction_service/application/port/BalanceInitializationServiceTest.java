package br.com.itau.transaction_service.application.port;

import br.com.itau.transaction_service.application.port.out.AccountRepositoryPort;
import br.com.itau.transaction_service.application.port.out.PublishEventPort;
import br.com.itau.transaction_service.domain.entity.Account;
import br.com.itau.transaction_service.domain.entity.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BalanceInitializationServiceTest {

    private AccountRepositoryPort repository;
    private PublishEventPort publishEventPort;
    private BalanceInitializationService service;

    @BeforeEach
    void setUp() {
        repository = mock(AccountRepositoryPort.class);
        publishEventPort = mock(PublishEventPort.class);
        service = new BalanceInitializationService(repository, publishEventPort);
    }

    @Test
    @DisplayName("Should initialize balance and publish event when account does not exist")
    void shouldInitializeBalanceSuccessfully() {
        UUID accountId = UUID.randomUUID();
        BigDecimal initialBalance = BigDecimal.ZERO;

        when(repository.loadById(accountId)).thenReturn(Optional.empty());

        service.execute(accountId, initialBalance);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(repository).createBalance(accountCaptor.capture());

        Account capturedAccount = accountCaptor.getValue();
        assertEquals(accountId, capturedAccount.getId());
        assertEquals(initialBalance, capturedAccount.getBalance());
        assertEquals(AccountStatus.ACTIVE, capturedAccount.getStatus());

        verify(publishEventPort).execute(capturedAccount);
    }

    @Test
    @DisplayName("Should do nothing if account balance already exists (Idempotency)")
    void shouldDoNothingIfAccountAlreadyExists() {
        UUID accountId = UUID.randomUUID();
        BigDecimal initialBalance = BigDecimal.TEN;
        Account existingAccount = new Account(accountId, BigDecimal.ZERO, AccountStatus.ACTIVE);

        when(repository.loadById(accountId)).thenReturn(Optional.of(existingAccount));

        service.execute(accountId, initialBalance);

        verify(repository, never()).createBalance(any());
        verify(publishEventPort, never()).execute(any());
    }
}