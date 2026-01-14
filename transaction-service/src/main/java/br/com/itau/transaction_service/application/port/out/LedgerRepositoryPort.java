package br.com.itau.transaction_service.application.port.out;

import br.com.itau.transaction_service.application.port.in.TransactionReceipt;
import br.com.itau.transaction_service.domain.entity.Transaction;

import java.util.Optional;

public interface LedgerRepositoryPort {

    void save(Transaction transaction);

    Optional<TransactionReceipt> findByIdempotencyKey(String key);

}
