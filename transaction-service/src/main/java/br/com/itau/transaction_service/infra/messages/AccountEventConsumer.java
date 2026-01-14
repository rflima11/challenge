package br.com.itau.transaction_service.infra.messages;

import br.com.itau.transaction_service.application.port.in.InitializeBalanceUseCase;
import br.com.itau.transaction_service.domain.entity.AccountCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AccountEventConsumer.class);
    private final InitializeBalanceUseCase initializeBalanceUseCase;
    private final ObjectMapper objectMapper;

    public AccountEventConsumer(InitializeBalanceUseCase initializeBalanceUseCase,
                                ObjectMapper objectMapper) {
        this.initializeBalanceUseCase = initializeBalanceUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "account-v1",
            groupId = "transaction-service-group"
    )
    public void handleAccountEvent(
            @Payload String message,
            @Header(name = "eventType") String eventType) {
        try {
            log.info("Recebendo evento de conta: {}", message);
            AccountCreatedEvent event = objectMapper.readValue(message, AccountCreatedEvent.class);
            if ("ACCOUNT_CREATED".equals(eventType)) {
                initializeBalanceUseCase.execute(UUID.fromString(event.accountId()), event.initialBalance());
                log.info("Processamento de saldo inicial concluído para conta: {}", event.accountId());
            }
        } catch (Exception e) {
            log.error("Erro ao processar evento de conta: {}", message, e);
        }
    }
}
