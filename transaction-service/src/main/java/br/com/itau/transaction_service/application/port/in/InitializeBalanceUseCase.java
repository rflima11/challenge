package br.com.itau.transaction_service.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface InitializeBalanceUseCase {
    void execute(UUID accountId, BigDecimal initialBalance);
}
