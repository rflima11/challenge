package br.com.itau.account_service.infra.messages;

import br.com.itau.account_service.application.port.out.PublishEventPort;
import br.com.itau.account_service.domain.entity.AccountCreatedEvent;
import br.com.itau.account_service.infra.config.properties.KafkaTopicsProperties;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventAdapter implements PublishEventPort {

    private final KafkaProducerHelper producerHelper;
    private final KafkaTopicsProperties properties;

    public KafkaEventAdapter(KafkaProducerHelper producerHelper, KafkaTopicsProperties properties) {
        this.producerHelper = producerHelper;
        this.properties = properties;
    }

    @Override
    public void execute(AccountCreatedEvent event) {
        String topic = properties.getAccounts().getName();
        String key = event.accountId();
        producerHelper.send(topic, key, event, "ACCOUNT_CREATED");
    }
}
