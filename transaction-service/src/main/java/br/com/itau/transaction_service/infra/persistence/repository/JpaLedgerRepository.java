package br.com.itau.transaction_service.infra.persistence.repository;

import br.com.itau.transaction_service.infra.persistence.entity.TransactionLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaLedgerRepository extends JpaRepository<TransactionLedgerEntity, UUID>{
    Optional<TransactionLedgerEntity> findByIdempotencyKey(String idempotencyKey);
}
