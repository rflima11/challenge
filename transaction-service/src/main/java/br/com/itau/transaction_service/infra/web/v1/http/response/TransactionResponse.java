package br.com.itau.transaction_service.infra.web.v1.http.response;

import br.com.itau.transaction_service.domain.entity.TransactionStatus;

public record TransactionResponse(
        String status
) {

    public static TransactionResponse from(
            TransactionStatus status) {
        return new TransactionResponse(status.getStatus());
    }
}
