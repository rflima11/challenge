package br.com.itau.transaction_service.application.port.out;

import br.com.itau.transaction_service.domain.entity.Account;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    Optional<Account> loadById(UUID id);
    void updateBalance(Account account);
    void createBalance(Account account);
}
