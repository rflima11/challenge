package br.com.itau.account_service.infra.persistence.entity;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED
}
