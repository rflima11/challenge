package br.com.itau.account_service.application.port.in;

import br.com.itau.account_service.domain.entity.Account;
import br.com.itau.account_service.domain.entity.AccountStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAccountCommand(
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String email,
        String telefone
) {

    public Account toAccount(
            UUID id,
            AccountStatus status,
            LocalDateTime createdAt
    ) {
        return new Account(
                id,
                status,
                nome,
                cpf,
                dataNascimento,
                telefone,
                email,
                createdAt
        );
    }

}
