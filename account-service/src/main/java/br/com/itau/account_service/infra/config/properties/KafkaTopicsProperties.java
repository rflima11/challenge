package br.com.itau.account_service.infra.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "account.kafka.topics")
public class KafkaTopicsProperties {

    private TopicConfig accounts;


    public TopicConfig getAccounts() {
        return accounts;
    }

    public void setAccounts(TopicConfig accounts) {
        this.accounts = accounts;
    }

    public static class TopicConfig {
        private String name;
        private int partitions;
        private int replicas;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getPartitions() { return partitions; }
        public void setPartitions(int partitions) { this.partitions = partitions; }
        public int getReplicas() { return replicas; }
        public void setReplicas(int replicas) { this.replicas = replicas; }
    }
}
