package br.com.itau.account_service.infra.web.v1;

import br.com.itau.account_service.application.port.in.CreateAccountUseCase;
import br.com.itau.account_service.domain.entity.Account;
import br.com.itau.account_service.infra.web.v1.http.request.CreateAccountRequest;
import br.com.itau.account_service.infra.web.v1.http.response.CreateAccountResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
public class AccountApi {

    private final CreateAccountUseCase createAccountUseCase;

    public AccountApi(CreateAccountUseCase createAccountUseCase) {
        this.createAccountUseCase = createAccountUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account accountCreated = createAccountUseCase.create(request.toCommand());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                CreateAccountResponse.from(accountCreated)
        );
    }

}
