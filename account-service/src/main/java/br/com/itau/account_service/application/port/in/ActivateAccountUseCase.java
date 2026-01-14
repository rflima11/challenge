package br.com.itau.account_service.application.port.in;

import java.util.UUID;

public interface ActivateAccountUseCase {

    void execute(UUID accountId);
}
