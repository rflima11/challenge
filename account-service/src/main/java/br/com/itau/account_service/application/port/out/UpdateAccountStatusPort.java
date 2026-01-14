package br.com.itau.account_service.application.port.out;

import br.com.itau.account_service.domain.entity.Account;

public interface UpdateAccountStatusPort {
    void execute(Account account);
}
