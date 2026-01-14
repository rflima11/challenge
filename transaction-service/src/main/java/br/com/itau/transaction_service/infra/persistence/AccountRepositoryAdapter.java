package br.com.itau.transaction_service.infra.persistence;

import br.com.itau.transaction_service.application.port.out.AccountRepositoryPort;
import br.com.itau.transaction_service.domain.entity.Account;
import br.com.itau.transaction_service.infra.persistence.entity.AccountBalanceEntity;
import br.com.itau.transaction_service.infra.persistence.repository.JpaAccountBalanceRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final JpaAccountBalanceRepository jpaAccountBalanceRepository;

    public AccountRepositoryAdapter(JpaAccountBalanceRepository jpaAccountBalanceRepository) {
        this.jpaAccountBalanceRepository = jpaAccountBalanceRepository;
    }

    @Override
    public Optional<Account> loadById(UUID id) {
        return jpaAccountBalanceRepository.findByIdWithLock(id)
                .map(entity -> new Account(entity.getAccountId(),
                        entity.getBalance(),
                        entity.getStatus()));
    }

    @Override
    public void updateBalance(Account account) {
        AccountBalanceEntity entity = jpaAccountBalanceRepository.findById(account.getId())
                .orElseThrow(() -> new RuntimeException("Inconsistency: Account vanished"));
        entity.setBalance(account.getBalance());
        jpaAccountBalanceRepository.save(entity);
    }

    @Override
    public void createBalance(Account account) {
        jpaAccountBalanceRepository.save(AccountBalanceEntity.createNewAccount(account));
    }
}
