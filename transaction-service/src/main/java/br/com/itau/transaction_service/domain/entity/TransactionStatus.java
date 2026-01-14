package br.com.itau.transaction_service.domain.entity;

public enum TransactionStatus {

    SUCCESS("EFETUADO"),
    RECUSED("RECUSADO");

    private String status;

    TransactionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
