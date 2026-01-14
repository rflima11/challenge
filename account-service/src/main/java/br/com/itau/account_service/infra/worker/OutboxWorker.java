package br.com.itau.account_service.infra.worker;

import br.com.itau.account_service.application.port.out.PublishEventPort;
import br.com.itau.account_service.domain.entity.AccountCreatedEvent;
import br.com.itau.account_service.infra.persistence.entity.OutboxEntity;
import br.com.itau.account_service.infra.persistence.entity.OutboxStatus;
import br.com.itau.account_service.infra.persistence.repository.JpaOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxWorker.class);

    private final JpaOutboxRepository outboxRepository;
    private final PublishEventPort publishEventPort;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public OutboxWorker(JpaOutboxRepository outboxRepository,
                        PublishEventPort publishEventPort,
                        ObjectMapper objectMapper,
                        TransactionTemplate transactionTemplate) {
        this.outboxRepository = outboxRepository;
        this.publishEventPort = publishEventPort;
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(fixedDelayString = "${outbox.worker.fixed-delay-ms:100}")
    public void processOutboxMessages() {
        List<OutboxEntity> batch = reserveBatch();
        if (batch.isEmpty()) {
            return;
        }
        log.info("Processing outbox batch with {} messages.", batch.size());
        List<UUID> successfulIds = new ArrayList<>();
        List<UUID> failedIds = new ArrayList<>();
        for (OutboxEntity message : batch) {
            try {
                processSingleMessage(message);
                successfulIds.add(message.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox message ID: {}: {}", message.getId(), e.getMessage(), e);
                failedIds.add(message.getId());
            }
        }
        updateBatchStatus(successfulIds, OutboxStatus.PROCESSED);
        updateBatchStatus(failedIds, OutboxStatus.FAILED);
        if (!failedIds.isEmpty()) {
            log.warn("Outbox batch completed with failures. Success: {}, Failed: {}", successfulIds.size(), failedIds.size());
        }
    }

    private List<OutboxEntity> reserveBatch() {
        return transactionTemplate.execute(status -> {
            List<OutboxEntity> messages = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

            if (messages.isEmpty()) {
                return Collections.emptyList();
            }

            messages.forEach(msg -> msg.setStatus(OutboxStatus.PROCESSING));
            return outboxRepository.saveAll(messages);
        });
    }

    private void processSingleMessage(OutboxEntity message) throws JsonProcessingException {
        AccountCreatedEvent payload = objectMapper.readValue(
                message.getPayload(),
                AccountCreatedEvent.class
        );
        publishEventPort.execute(payload);
    }

    private void updateBatchStatus(List<UUID> ids, OutboxStatus status) {
        if (ids.isEmpty()) {
            return;
        }

        transactionTemplate.execute(tx -> {
            outboxRepository.updateStatusBatch(ids, status);
            return null;
        });
    }
}