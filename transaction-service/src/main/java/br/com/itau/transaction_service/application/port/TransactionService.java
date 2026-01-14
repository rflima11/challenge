package br.com.itau.transaction_service.application.port;

import br.com.itau.transaction_service.application.port.in.CreateTransactionCommand;
import br.com.itau.transaction_service.application.port.in.CreateTransactionUseCase;
import br.com.itau.transaction_service.application.port.in.TransactionReceipt;
import br.com.itau.transaction_service.application.port.out.AccountRepositoryPort;
import br.com.itau.transaction_service.application.port.out.LedgerRepositoryPort;
import br.com.itau.transaction_service.domain.entity.Account;
import br.com.itau.transaction_service.domain.entity.Transaction;
import br.com.itau.transaction_service.domain.entity.TransactionType;
import br.com.itau.transaction_service.domain.exception.TransactionErrorException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService implements CreateTransactionUseCase {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final AccountRepositoryPort accountRepository;
    private final LedgerRepositoryPort ledgerRepository;

    public TransactionService(AccountRepositoryPort accountRepository,
                              LedgerRepositoryPort ledgerRepository) {
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional
    @Override
    public TransactionReceipt execute(CreateTransactionCommand command) {
        try {
            Optional<TransactionReceipt> existing = ledgerRepository.findByIdempotencyKey(command.idempotencyKey());
            if (existing.isPresent()) {
                return existing.get();
            }
            Account account = accountRepository.loadById(command.accountId())
                    .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + command.accountId()));

            Transaction transaction = new Transaction(
                    UUID.randomUUID(),
                    account,
                    command.type(),
                    command.amount(),
                    LocalDateTime.now(),
                    command.idempotencyKey()
            );

            if (transaction.getType() == TransactionType.WITHDRAWAL) {
                account.withdraw(transaction.getAmount());
            } else {
                account.deposit(transaction.getAmount());
            }
            transaction.conclude(account.getBalance());
            accountRepository.updateBalance(account);
            ledgerRepository.save(transaction);
            return TransactionReceipt.from(transaction);
        } catch (Exception e) {
            logger.error("Error processing transaction", e);
            throw new TransactionErrorException("Error processing transaction");
        }
    }


}
