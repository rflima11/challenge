package br.com.itau.account_service.application.port;

import br.com.itau.account_service.application.port.in.ActivateAccountUseCase;
import br.com.itau.account_service.application.port.out.FindAccountPort;
import br.com.itau.account_service.application.port.out.UpdateAccountStatusPort;
import br.com.itau.account_service.domain.entity.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ActivateAccountService implements ActivateAccountUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActivateAccountService.class);

    private final FindAccountPort findAccountPort;
    private final UpdateAccountStatusPort updateAccountStatusPort;

    public ActivateAccountService(FindAccountPort findAccountPort,
                                  UpdateAccountStatusPort updateAccountStatusPort) {
        this.findAccountPort = findAccountPort;
        this.updateAccountStatusPort = updateAccountStatusPort;
    }

    @Override
    public void execute(UUID accountId) {
        Account account = findAccountPort.execute(accountId.toString()).orElse(null);

        if (account == null) {
            log.warn("Account with id {} not found for activation.", accountId);
            return;
        }
        account.activate();
        updateAccountStatusPort.execute(account);
    }
}
