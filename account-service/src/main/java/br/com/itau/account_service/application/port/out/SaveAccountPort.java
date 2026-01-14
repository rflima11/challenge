package br.com.itau.account_service.application.port.out;

import br.com.itau.account_service.domain.entity.Account;

public interface SaveAccountPort {
    Account execute(Account account);
}
