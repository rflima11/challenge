package br.com.itau.account_service.infra.web.v1;


import br.com.itau.account_service.application.port.in.CreateAccountCommand;
import br.com.itau.account_service.application.port.in.CreateAccountUseCase;
import br.com.itau.account_service.domain.entity.Account;
import br.com.itau.account_service.domain.entity.AccountStatus;
import br.com.itau.account_service.infra.web.v1.http.request.CreateAccountRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountApi.class)
class AccountApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateAccountUseCase createAccountUseCase;

    @Test
    @DisplayName("Should create account successfully (Return 202 Accepted)")
    void shouldCreateAccountSuccessfully() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                "Rodolfo Lima",
                "04747103198",
                LocalDate.of(1990, 1, 1),
                "rodolfo@teste.com",
                "5511999998888"
        );

        Account dummyAccount = new Account(
                UUID.randomUUID(),
                AccountStatus.PENDING,
                "Rodolfo Lima",
                "04747103198",
                LocalDate.now().minusYears(30),
                "5511999998888",
                "rodolfo@teste.com",
                LocalDateTime.now()
        );


        when(createAccountUseCase.create(any(CreateAccountCommand.class)))
                .thenReturn(dummyAccount);

        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.accountId").exists())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when Name is invalid (Blank or Short)")
    void shouldReturnBadRequestWhenNameIsInvalid() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                "Ro",
                "04747103198",
                LocalDate.of(1990, 1, 1),
                "rodolfo@teste.com",
                "5511999998888"
        );

        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when CPF is invalid format")
    void shouldReturnBadRequestWhenCpfIsInvalid() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                "Rodolfo Lima",
                "12345678900",
                LocalDate.of(1990, 1, 1),
                "rodolfo@teste.com",
                "5511999998888"
        );

        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when Birth Date is in the future")
    void shouldReturnBadRequestWhenDateIsFuture() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                "Rodolfo Lima",
                "04747103198",
                LocalDate.now().plusDays(1),
                "rodolfo@teste.com",
                "5511999998888"
        );

        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when Email is invalid")
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                "Rodolfo Lima",
                "04747103198",
                LocalDate.now().minusYears(18),
                "not-an-email",
                "5511999998888"
        );

        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when Phone is invalid regex")
    void shouldReturnBadRequestWhenPhoneIsInvalid() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                "Rodolfo Lima",
                "04747103198",
                LocalDate.now().minusYears(18),
                "rodolfo@teste.com",
                "123"
        );

        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}