package br.com.itau.transaction_service.application.port.in;

public interface CreateTransactionUseCase {

    TransactionReceipt execute(CreateTransactionCommand command);
}
