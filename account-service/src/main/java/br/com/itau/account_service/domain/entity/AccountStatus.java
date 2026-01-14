package br.com.itau.account_service.domain.entity;

public enum AccountStatus {


    PENDING("PENDING"),
    ACTIVE("ACTIVE"),
    SUSPENDED("SUSPENDED"),
    CLOSED("CLOSED");

    private String status;

    AccountStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

