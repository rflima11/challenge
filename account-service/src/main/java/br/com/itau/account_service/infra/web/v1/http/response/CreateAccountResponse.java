package br.com.itau.account_service.infra.web.v1.http.response;

import br.com.itau.account_service.domain.entity.Account;

public record CreateAccountResponse(
        String accountId,
        String status
) {

    public static CreateAccountResponse from(
            Account account) {
        return new CreateAccountResponse(account.getId().toString(),
                                            account.getStatus().getStatus());
    }

}