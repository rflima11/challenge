package br.com.itau.account_service.infra.web.v1.http.request;

import br.com.itau.account_service.application.port.in.CreateAccountCommand;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record CreateAccountRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
        String nome,

        @NotBlank(message = "O CPF é obrigatório")
        @CPF(message = "CPF inválido")
        String cpf,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve estar no passado")
        LocalDate dataNascimento,

        @Email(message = "Formato de e-mail inválido")
        String email,

        @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Telefone inválido")
        String telefone
) {

        public CreateAccountCommand toCommand() {
            return new CreateAccountCommand(
                    this.nome,
                    this.cpf,
                    this.dataNascimento,
                    this.email,
                    this.telefone
            );
        }

}
