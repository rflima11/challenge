package br.com.itau.transaction_service.infra.web.v1.http.request;

import br.com.itau.transaction_service.application.port.in.CreateTransactionCommand;
import br.com.itau.transaction_service.domain.entity.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRequest(
        @NotBlank(message = "O accountId é obrigatório")
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "O accountId deve ser um UUID válido"
        )
        String accountId,
        @NotBlank(message = "O tipo de operação é obrigatório")
        @Pattern(regexp = "DEPOSIT|WITHDRAWAL", message = "Operação inválida. Use DEPOSIT ou WITHDRAWAL")
        String operationType,
        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor da transação deve ser maior que zero")
        @Digits(integer = 13, fraction = 2, message = "Formato de valor inválido (max 13 digitos inteiros e 2 decimais)")
        BigDecimal amount
) {
    public CreateTransactionCommand toCommand(String idempotencyKey) {
        return new CreateTransactionCommand(
                UUID.fromString(accountId),
                amount,
                TransactionType.fromOperationType(operationType),
                idempotencyKey
        );
    }
}
