package br.com.itau.account_service.application.port.out;

import br.com.itau.account_service.domain.entity.AccountCreatedEvent;

public interface PublishEventPort {

    void execute(AccountCreatedEvent event);

}
