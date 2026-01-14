package br.com.itau.transaction_service.infra.persistence.repository;

import br.com.itau.transaction_service.infra.persistence.entity.AccountBalanceEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;
import java.util.UUID;

public interface JpaAccountBalanceRepository extends JpaRepository<AccountBalanceEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountBalanceEntity a WHERE a.accountId = :id")
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")
    })
    Optional<AccountBalanceEntity> findByIdWithLock(UUID id);

}
