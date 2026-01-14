package br.com.itau.account_service.infra.messages;

import br.com.itau.account_service.application.port.in.ActivateAccountUseCase;
import br.com.itau.account_service.infra.messages.dto.BalanceCreatedEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BalanceConsumer {

    private static final Logger log = LoggerFactory.getLogger(BalanceConsumer.class);
    private final ObjectMapper objectMapper;
    private final ActivateAccountUseCase activateAccountUseCase;

    public BalanceConsumer(ObjectMapper objectMapper,
                           ActivateAccountUseCase activateAccountUseCase) {
        this.objectMapper = objectMapper;
        this.activateAccountUseCase = activateAccountUseCase;
    }

    @KafkaListener(
            topics = "balance-v1",
            groupId = "account-service-group"
    )
    public void handleAccountEvent(
            @Payload String message,
            @Header("eventType") String eventType) {
        try {
            BalanceCreatedEventDTO event = objectMapper.readValue(message, BalanceCreatedEventDTO.class);
            if ("BALANCE_INITIALIZED".equals(eventType)) {
                activateAccountUseCase.execute(UUID.fromString(event.id()));
            }
        } catch (Exception e) {
            log.error("Error processing balance event from Kafka. Event Type: {}, Error: {}", eventType, e.getMessage(), e);
        }
    }
}
