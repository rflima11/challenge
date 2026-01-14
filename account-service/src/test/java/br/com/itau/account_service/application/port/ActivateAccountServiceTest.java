package br.com.itau.account_service.application.port;

import br.com.itau.account_service.application.port.out.FindAccountPort;
import br.com.itau.account_service.application.port.out.UpdateAccountStatusPort;
import br.com.itau.account_service.domain.entity.Account;
import br.com.itau.account_service.domain.entity.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ActivateAccountServiceTest {

    private FindAccountPort findAccountPort;
    private UpdateAccountStatusPort updateAccountStatusPort;
    private ActivateAccountService service;

    @BeforeEach
    void setUp() {
        findAccountPort = Mockito.mock(FindAccountPort.class);
        updateAccountStatusPort = Mockito.mock(UpdateAccountStatusPort.class);
        service = new ActivateAccountService(findAccountPort, updateAccountStatusPort);
    }

    @Test
    void shouldActivateAccountSuccessfully() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(
                UUID.randomUUID(),
                AccountStatus.PENDING,
                "Jane Doe",
                "04747103198",
                LocalDate.now().minusYears(25),
                "61981087257",
                "rodolfo@email.com",
                LocalDateTime.now()
        );


        when(findAccountPort.execute(accountId.toString())).thenReturn(Optional.of(account));

        service.execute(accountId);

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void shouldDoNothingWhenAccountNotFound() {
        UUID accountId = UUID.randomUUID();

        when(findAccountPort.execute(accountId.toString())).thenReturn(Optional.empty());

        service.execute(accountId);

        verify(updateAccountStatusPort, never()).execute(any());
    }


}