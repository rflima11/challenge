package br.com.itau.transaction_service.infra.persistence;

import br.com.itau.transaction_service.application.port.in.TransactionReceipt;
import br.com.itau.transaction_service.application.port.out.LedgerRepositoryPort;
import br.com.itau.transaction_service.domain.entity.Transaction;
import br.com.itau.transaction_service.infra.persistence.entity.TransactionLedgerEntity;
import br.com.itau.transaction_service.infra.persistence.repository.JpaLedgerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LedgerRepositoryAdapter implements LedgerRepositoryPort {

    private final JpaLedgerRepository jpaLedgerRepository;

    public LedgerRepositoryAdapter(JpaLedgerRepository jpaLedgerRepository) {
        this.jpaLedgerRepository = jpaLedgerRepository;
    }

    @Override
    public void save(Transaction transaction) {
        jpaLedgerRepository.save(TransactionLedgerEntity.fromTransaction(transaction));
    }

    @Override
    public Optional<TransactionReceipt> findByIdempotencyKey(String key) {
        return jpaLedgerRepository.findByIdempotencyKey(key).
                map(TransactionLedgerEntity::toTransactionReceipt);
    }
}
