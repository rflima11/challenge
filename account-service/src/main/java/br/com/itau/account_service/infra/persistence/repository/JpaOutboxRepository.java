package br.com.itau.account_service.infra.persistence.repository;

import br.com.itau.account_service.infra.persistence.entity.OutboxEntity;
import br.com.itau.account_service.infra.persistence.entity.OutboxStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.List;

public interface JpaOutboxRepository extends JpaRepository<OutboxEntity, UUID> {
    @QueryHints({
            @QueryHint(
                    name = "jakarta.persistence.lock.timeout",
                    value = "-2"
            )
    })
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEntity> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE OutboxEntity o SET o.status = :status WHERE o.id IN :ids")
    void updateStatusBatch(@Param("ids") List<UUID> ids, @Param("status") OutboxStatus status);
}
