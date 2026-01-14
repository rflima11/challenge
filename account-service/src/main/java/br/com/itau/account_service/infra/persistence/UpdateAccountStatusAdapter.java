package br.com.itau.account_service.infra.persistence;

import br.com.itau.account_service.application.port.out.UpdateAccountStatusPort;
import br.com.itau.account_service.domain.entity.Account;
import br.com.itau.account_service.infra.persistence.repository.JpaAccountRepository;
import org.springframework.stereotype.Component;

@Component
public class UpdateAccountStatusAdapter implements UpdateAccountStatusPort {

    private final JpaAccountRepository jpaAccountRepository;

    public UpdateAccountStatusAdapter(JpaAccountRepository jpaAccountRepository) {
        this.jpaAccountRepository = jpaAccountRepository;
    }

    @Override
    public void execute(Account account) {
        jpaAccountRepository.updateStatus(account.getId(), account.getStatus());
    }
}
