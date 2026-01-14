package br.com.itau.account_service.application.port;

import br.com.itau.account_service.application.port.in.CreateAccountCommand;
import br.com.itau.account_service.application.port.out.SaveAccountPort;
import br.com.itau.account_service.domain.entity.Account;
import br.com.itau.account_service.domain.entity.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private SaveAccountPort saveAccountPort;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        saveAccountPort = mock(SaveAccountPort.class);
        accountService = new AccountService(saveAccountPort);
    }

    @Test
    @DisplayName("Should create account successfully when user is adult")
    void shouldCreateAccountSuccessfully() {
        CreateAccountCommand command = new CreateAccountCommand(
                "John Doe",
                "04747103198",
                LocalDate.now().minusYears(19),
                "rodolfo@email.com",
                "61981087257"
        );

        when(saveAccountPort.execute(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account createdAccount = accountService.create(command);

        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getId()).isNotNull();
        assertThat(createdAccount.getStatus()).isEqualTo(AccountStatus.PENDING);
        assertThat(createdAccount.getCreatedAt()).isNotNull();

        verify(saveAccountPort, times(1)).execute(any(Account.class));
    }

    @Test
    @DisplayName("Should throw exception when user is underage")
    void shouldThrowExceptionWhenUnderage() {
        CreateAccountCommand command = new CreateAccountCommand(
                "Kid Doe",
                "04747103198",
                LocalDate.now().minusYears(17),
                "rodolfo@email.com",
                "61981087257"
        );

        assertThatThrownBy(() -> accountService.create(command))
                .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(saveAccountPort);
    }
}