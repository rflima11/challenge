package br.com.itau.account_service.application.port.out;

import br.com.itau.account_service.domain.entity.Account;

import java.util.Optional;

public interface FindAccountPort {
    Optional<Account> execute(String accountId);
}
