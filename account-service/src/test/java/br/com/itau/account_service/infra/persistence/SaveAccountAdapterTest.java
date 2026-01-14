package br.com.itau.account_service.infra.persistence;

import br.com.itau.account_service.domain.entity.Account;
import br.com.itau.account_service.domain.entity.AccountStatus;
import br.com.itau.account_service.domain.exception.AccountAlreadyExistsException;
import br.com.itau.account_service.infra.config.properties.KafkaTopicsProperties;
import br.com.itau.account_service.infra.config.properties.KafkaTopicsProperties.TopicConfig;
import br.com.itau.account_service.infra.persistence.entity.AccountEntity;
import br.com.itau.account_service.infra.persistence.entity.OutboxEntity;
import br.com.itau.account_service.infra.persistence.entity.OutboxStatus;
import br.com.itau.account_service.infra.persistence.repository.JpaAccountRepository;
import br.com.itau.account_service.infra.persistence.repository.JpaOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(SaveAccountAdapter.class)
@ActiveProfiles("test")
class SaveAccountAdapterTest {

    @Autowired
    private SaveAccountAdapter adapter;

    @Autowired
    private JpaAccountRepository accountRepository;

    @Autowired
    private JpaOutboxRepository outboxRepository;

    @TestConfiguration
    static class Config {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper;
        }

        @Bean
        public KafkaTopicsProperties kafkaTopicsProperties() {
            KafkaTopicsProperties props = Mockito.mock(KafkaTopicsProperties.class);
            TopicConfig topicConfig = Mockito.mock(TopicConfig.class);
            when(props.getAccounts()).thenReturn(topicConfig);
            when(topicConfig.getName()).thenReturn("account-created-topic");
            return props;
        }
    }

    @Test
    @DisplayName("Should persist Account and Outbox event in the H2 database")
    void shouldPersistAccountAndOutbox() {
        Account account = new Account(
                UUID.randomUUID(),
                AccountStatus.PENDING,
                "Jane Doe",
                "04747103198",
                LocalDate.now().minusYears(25),
                "61981087257",
                "rodolfo@email.com",
                LocalDateTime.now()
        );


        Account savedAccount = adapter.execute(account);

        assertNotNull(savedAccount.getId());
        assertEquals("Jane Doe", savedAccount.getOwnerName());
        Optional<AccountEntity> entityFound = accountRepository.findById(savedAccount.getId());
        assertTrue(entityFound.isPresent());
        assertEquals("04747103198", entityFound.get().getOwnerDocument());
        List<OutboxEntity> outboxMessages = outboxRepository.findAll();
        assertEquals(1, outboxMessages.size());
        assertEquals("account-created-topic", outboxMessages.get(0).getTopic());
        assertEquals(OutboxStatus.PENDING, outboxMessages.get(0).getStatus());
        assertTrue(outboxMessages.get(0).getPayload().contains(savedAccount.getId().toString()));
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolation (or custom ex) when document is duplicated")
    void shouldFailWhenDocumentDuplicated() {
        Account account1 = new Account(
                UUID.randomUUID(),
                AccountStatus.PENDING,
                "Jane Doe",
                "04747103198",
                LocalDate.now().minusYears(25),
                "61981087257",
                "rodolfo@email.com",
                LocalDateTime.now()
        );

        adapter.execute(account1);

        Account account2 = new Account(
                UUID.randomUUID(),
                AccountStatus.PENDING,
                "Jane Doe",
                "04747103198",
                LocalDate.now().minusYears(25),
                "61981087257",
                "rodolfo@email.com",
                LocalDateTime.now()
        );
        Exception exception = assertThrows(Exception.class, () -> adapter.execute(account2));

        assertTrue(
                exception instanceof DataIntegrityViolationException ||
                        exception instanceof AccountAlreadyExistsException
        );
    }

}