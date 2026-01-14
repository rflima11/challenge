package br.com.itau.account_service.infra.persistence.repository;

import br.com.itau.account_service.domain.entity.AccountStatus;
import br.com.itau.account_service.infra.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaAccountRepository extends JpaRepository<AccountEntity, UUID> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE AccountEntity o SET o.status = :status WHERE o.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") AccountStatus status);
}
