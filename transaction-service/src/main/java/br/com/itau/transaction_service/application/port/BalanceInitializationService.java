package br.com.itau.transaction_service.application.port;

import br.com.itau.transaction_service.application.port.in.InitializeBalanceUseCase;
import br.com.itau.transaction_service.application.port.out.AccountRepositoryPort;
import br.com.itau.transaction_service.application.port.out.PublishEventPort;
import br.com.itau.transaction_service.domain.entity.Account;
import br.com.itau.transaction_service.domain.entity.AccountStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BalanceInitializationService implements InitializeBalanceUseCase {

    private final AccountRepositoryPort repository;
    private final PublishEventPort publishEventPort;

    public BalanceInitializationService(AccountRepositoryPort repository, PublishEventPort publishEventPort) {
        this.repository = repository;
        this.publishEventPort = publishEventPort;
    }

    @Override
    @Transactional
    public void execute(UUID accountId, BigDecimal initialBalance) {
        if (repository.loadById(accountId).isPresent()) {
            return;
        }
        Account newBalance = new Account(
                accountId,
                initialBalance,
                AccountStatus.ACTIVE
        );
        repository.createBalance(newBalance);
        publishEventPort.execute(newBalance);
    }
}
