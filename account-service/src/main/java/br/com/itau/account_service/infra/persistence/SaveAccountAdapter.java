package br.com.itau.account_service.infra.persistence;

import br.com.itau.account_service.application.port.out.FindAccountPort;
import br.com.itau.account_service.application.port.out.SaveAccountPort;
import br.com.itau.account_service.domain.entity.Account;
import br.com.itau.account_service.domain.entity.AccountCreatedEvent;
import br.com.itau.account_service.domain.exception.AccountAlreadyExistsException;
import br.com.itau.account_service.infra.config.properties.KafkaTopicsProperties;
import br.com.itau.account_service.infra.persistence.entity.AccountEntity;
import br.com.itau.account_service.infra.persistence.entity.OutboxEntity;
import br.com.itau.account_service.infra.persistence.entity.OutboxStatus;
import br.com.itau.account_service.infra.persistence.repository.JpaAccountRepository;
import br.com.itau.account_service.infra.persistence.repository.JpaOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class SaveAccountAdapter implements SaveAccountPort, FindAccountPort {

    private static final Logger log = LoggerFactory.getLogger(SaveAccountAdapter.class);
    private final JpaAccountRepository accountRepository;
    private final JpaOutboxRepository outboxRepository;
    private final KafkaTopicsProperties kafkaTopicsProperties;
    private final ObjectMapper objectMapper;

    private static final String INDEX_ACTIVE_ACCOUNT_UNIQUE = "uk_account_document_active";

    public SaveAccountAdapter(JpaAccountRepository accountRepository, JpaOutboxRepository outboxRepository, KafkaTopicsProperties kafkaTopicsProperties, ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.outboxRepository = outboxRepository;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
        this.objectMapper = objectMapper;
    }

    @Transactional(timeout = 10)
    @Override
    public Account execute(Account account) {
        try {
            Account createdAccount = accountRepository.saveAndFlush(AccountEntity.fromDomain(account)).toDomain();
            String eventPayload = objectMapper.writeValueAsString(
                    new AccountCreatedEvent(createdAccount.getId().toString(), BigDecimal.ZERO, createdAccount.getCreatedAt()
            ));
            outboxRepository.save(new OutboxEntity(kafkaTopicsProperties.getAccounts().getName(), eventPayload, OutboxStatus.PENDING));
            return createdAccount;
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains(INDEX_ACTIVE_ACCOUNT_UNIQUE)) {
                log.warn("Failed to save account: duplicate active account detected for document");
                throw new AccountAlreadyExistsException("The provide" +
                        "d document already has an active account registered.");
            }
            log.error("Error saving account with ID: {}", account.getId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while saving account with ID: {}", account.getId(), e);
            throw new RuntimeException("Failed to save account or create outbox message", e);
        }
    }

    @Override
    public Optional<Account> execute(String accountId) {
        return accountRepository.findById(UUID.fromString(accountId)).map(AccountEntity::toDomain);
    }
}
