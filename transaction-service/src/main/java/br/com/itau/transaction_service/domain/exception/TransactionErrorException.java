package br.com.itau.transaction_service.domain.exception;

public class TransactionErrorException extends RuntimeException {
    public TransactionErrorException(String message) {
        super(message);
    }
}
