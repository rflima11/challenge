package br.com.itau.transaction_service.infra.messages;

import br.com.itau.transaction_service.application.port.out.PublishEventPort;
import br.com.itau.transaction_service.domain.entity.Account;
import br.com.itau.transaction_service.infra.messages.properties.KafkaTopicsProperties;
import org.springframework.stereotype.Component;

@Component
public class KafkaPublishEventAdapter implements PublishEventPort {

    private final KafkaProducerHelper producerHelper;
    private final KafkaTopicsProperties properties;


    public KafkaPublishEventAdapter(KafkaProducerHelper producerHelper, KafkaTopicsProperties properties) {
        this.producerHelper = producerHelper;
        this.properties = properties;
    }

    @Override
    public void execute(Account accountEvent) {
        String topic = properties.getAccounts().getName();
        String key = accountEvent.getId().toString();
        producerHelper.send(topic, key, accountEvent, "BALANCE_INITIALIZED");
    }
}
