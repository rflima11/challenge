package br.com.itau.account_service.infra.config;

import br.com.itau.account_service.infra.config.properties.KafkaTopicsProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic accountTopic(KafkaTopicsProperties props) {
        var config = props.getAccounts();
        return TopicBuilder.name(config.getName())
                .partitions(config.getPartitions())
                .replicas(config.getReplicas())
                .build();
    }

}
