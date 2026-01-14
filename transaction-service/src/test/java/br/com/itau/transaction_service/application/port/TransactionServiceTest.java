package br.com.itau.transaction_service.application.port;

import br.com.itau.transaction_service.application.port.in.CreateTransactionCommand;
import br.com.itau.transaction_service.application.port.in.TransactionReceipt;
import br.com.itau.transaction_service.application.port.out.AccountRepositoryPort;
import br.com.itau.transaction_service.application.port.out.LedgerRepositoryPort;
import br.com.itau.transaction_service.domain.entity.*;
import br.com.itau.transaction_service.domain.exception.TransactionErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private AccountRepositoryPort accountRepository;

    private LedgerRepositoryPort ledgerRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepositoryPort.class);
        ledgerRepository = mock(LedgerRepositoryPort.class);
        transactionService = new TransactionService(accountRepository, ledgerRepository);
    }

    @Test
    @DisplayName("Should process deposit successfully: update balance and save transaction")
    void shouldProcessDepositSuccessfully() {
        UUID accountId = UUID.randomUUID();
        String idempotencyKey = "key-123";
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal depositAmount = new BigDecimal("50.00");
        BigDecimal expectedFinalBalance = new BigDecimal("150.00");

        Account account = new Account(accountId, initialBalance, AccountStatus.ACTIVE);

        CreateTransactionCommand command = new CreateTransactionCommand(
                accountId,
                depositAmount,
                TransactionType.DEPOSIT,
                idempotencyKey
        );

        when(ledgerRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(accountRepository.loadById(accountId)).thenReturn(Optional.of(account));

        TransactionReceipt receipt = transactionService.execute(command);

        assertNotNull(receipt);
        assertEquals(expectedFinalBalance, receipt.currentBalance());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).updateBalance(accountCaptor.capture());
        assertEquals(expectedFinalBalance, accountCaptor.getValue().getBalance());

        verify(ledgerRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should process withdrawal successfully when balance is sufficient")
    void shouldProcessWithdrawalSuccessfully() {
        UUID accountId = UUID.randomUUID();
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal withdrawAmount = new BigDecimal("40.00");

        Account account = new Account(accountId, initialBalance, AccountStatus.ACTIVE);

        CreateTransactionCommand command = new CreateTransactionCommand(
                accountId,
                withdrawAmount,
                TransactionType.WITHDRAWAL,
                "key-456"
        );

        when(ledgerRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.loadById(accountId)).thenReturn(Optional.of(account));

        TransactionReceipt receipt = transactionService.execute(command);

        assertEquals(new BigDecimal("60.00"), receipt.currentBalance());
        verify(accountRepository).updateBalance(any(Account.class));
        verify(ledgerRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should return existing receipt if idempotency key already exists")
    void shouldReturnExistingReceiptIfIdempotencyKeyExists() {
        String idempotencyKey = "duplicate-key";
        UUID transactionId = UUID.randomUUID();

        TransactionReceipt existingReceipt = new TransactionReceipt(
                transactionId,
                TransactionStatus.SUCCESS,
                new BigDecimal("10.00"),
                "DEPOSIT",
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                idempotencyKey
        );

        CreateTransactionCommand command = new CreateTransactionCommand(
                UUID.randomUUID(),
                BigDecimal.TEN,
                TransactionType.DEPOSIT,
                idempotencyKey
        );

        when(ledgerRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(existingReceipt));

        TransactionReceipt receipt = transactionService.execute(command);

        assertEquals(existingReceipt, receipt);
        assertEquals(transactionId, receipt.transactionId());

        verify(accountRepository, never()).loadById(any());
        verify(accountRepository, never()).updateBalance(any());
        verify(ledgerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw TransactionErrorException when account does not exist")
    void shouldThrowExceptionWhenAccountNotFound() {
        CreateTransactionCommand command = new CreateTransactionCommand(
                UUID.randomUUID(), BigDecimal.TEN, TransactionType.DEPOSIT, "key-999"
        );

        when(ledgerRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.loadById(any())).thenReturn(Optional.empty());

        assertThrows(TransactionErrorException.class, () -> transactionService.execute(command));

        verify(accountRepository, never()).updateBalance(any());
    }

    @Test
    @DisplayName("Should capture business error (insufficient funds) and rethrow as TransactionErrorException")
    void shouldThrowExceptionWhenInsufficientFunds() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(accountId, new BigDecimal("10.00"), AccountStatus.ACTIVE);

        CreateTransactionCommand command = new CreateTransactionCommand(
                accountId,
                new BigDecimal("50.00"),
                TransactionType.WITHDRAWAL,
                "key-low-funds"
        );

        when(ledgerRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.loadById(accountId)).thenReturn(Optional.of(account));

        assertThrows(TransactionErrorException.class, () -> transactionService.execute(command));

        verify(accountRepository, never()).updateBalance(any());
        verify(ledgerRepository, never()).save(any());
    }
}