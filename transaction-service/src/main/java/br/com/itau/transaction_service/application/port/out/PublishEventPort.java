package br.com.itau.transaction_service.application.port.out;

import br.com.itau.transaction_service.domain.entity.Account;

public interface PublishEventPort {
    void execute(Account accountEvent);
}
