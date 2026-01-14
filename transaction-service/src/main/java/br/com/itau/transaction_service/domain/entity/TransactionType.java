package br.com.itau.transaction_service.domain.entity;

import java.util.Arrays;

public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL;

    public static TransactionType fromOperationType(String operationType) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(operationType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid operation type: " + operationType));
    }
}
