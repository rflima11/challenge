package br.com.itau.account_service.application.port.in;

import br.com.itau.account_service.domain.entity.Account;

public interface CreateAccountUseCase {

    Account create(CreateAccountCommand command);

}
