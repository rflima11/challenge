package br.com.itau.transaction_service.infra.web.v1;

import br.com.itau.transaction_service.application.port.in.CreateTransactionUseCase;
import br.com.itau.transaction_service.application.port.in.TransactionReceipt;
import br.com.itau.transaction_service.infra.web.v1.http.request.TransactionRequest;
import br.com.itau.transaction_service.infra.web.v1.http.response.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid TransactionRequest request
    ) {
        TransactionReceipt transactionResult = createTransactionUseCase.execute(request.toCommand(idempotencyKey));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                TransactionResponse.from(transactionResult.status())
        );
    }
}
