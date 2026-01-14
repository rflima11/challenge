package br.com.itau.account_service.application.port;

import br.com.itau.account_service.application.port.in.CreateAccountCommand;
import br.com.itau.account_service.application.port.in.CreateAccountUseCase;
import br.com.itau.account_service.application.port.out.SaveAccountPort;
import br.com.itau.account_service.domain.entity.Account;
import br.com.itau.account_service.domain.entity.AccountStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccountService implements CreateAccountUseCase {

    private final SaveAccountPort saveAccountPort;
    private static final Integer MINIMUM_AGE = 18;

    public AccountService(SaveAccountPort saveAccountPort) {
        this.saveAccountPort = saveAccountPort;
    }

    @Override
    public Account create(CreateAccountCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateAccountCommand cannot be null");
        }

        Account account = command.toAccount(
                UUID.randomUUID(),
                AccountStatus.PENDING,
                LocalDateTime.now()
        );
        account.validateAge(MINIMUM_AGE);
        return saveAccountPort.execute(account);
    }
}
