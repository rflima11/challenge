package br.com.itau.transaction_service.infra.web.v1;


import br.com.itau.transaction_service.application.port.in.CreateTransactionUseCase;
import br.com.itau.transaction_service.application.port.in.TransactionReceipt;
import br.com.itau.transaction_service.domain.entity.TransactionStatus;
import br.com.itau.transaction_service.infra.web.v1.http.request.TransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateTransactionUseCase createTransactionUseCase;

    @Test
    @DisplayName("Should return 201 Created when request is valid")
    void shouldCreateTransactionSuccessfully() throws Exception {
        String validUUID = "550e8400-e29b-41d4-a716-446655440000";
        TransactionRequest request = new TransactionRequest(
                validUUID,
                "DEPOSIT",
                new BigDecimal("100.00")
        );

        TransactionReceipt mockReceipt = new TransactionReceipt(
                UUID.randomUUID(),
                TransactionStatus.SUCCESS,
                request.amount(),
                "DEPOSIT",
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                "key-123"
        );

        when(createTransactionUseCase.execute(any())).thenReturn(mockReceipt);

        mockMvc.perform(post("/v1/transactions")
                        .header("Idempotency-Key", "key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EFETUADO"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when Account ID is invalid UUID")
    void shouldReturn400WhenAccountIdIsInvalid() throws Exception {
        TransactionRequest request = new TransactionRequest(
                "invalid-uuid-123",
                "DEPOSIT",
                new BigDecimal("100.00")
        );

        mockMvc.perform(post("/v1/transactions")
                        .header("Idempotency-Key", "key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validations[?(@.field == 'accountId')]").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when Operation Type is invalid")
    void shouldReturn400WhenOperationTypeIsInvalid() throws Exception {
        TransactionRequest request = new TransactionRequest(
                UUID.randomUUID().toString(),
                "PIX",
                new BigDecimal("100.00")
        );

        mockMvc.perform(post("/v1/transactions")
                        .header("Idempotency-Key", "key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validations[?(@.field == 'operationType')]").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when Amount is negative")
    void shouldReturn400WhenAmountIsNegative() throws Exception {
        TransactionRequest request = new TransactionRequest(
                UUID.randomUUID().toString(),
                "WITHDRAWAL",
                new BigDecimal("-50.00")
        );

        mockMvc.perform(post("/v1/transactions")
                        .header("Idempotency-Key", "key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validations[?(@.field == 'amount')]").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when Header is missing")
    void shouldReturn400WhenHeaderIsMissing() throws Exception {
        TransactionRequest request = new TransactionRequest(
                UUID.randomUUID().toString(),
                "DEPOSIT",
                new BigDecimal("50.00")
        );

        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}