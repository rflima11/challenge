package br.com.itau.account_service.domain.entity;

import br.com.itau.account_service.domain.exception.InvalidAccountException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Account {

    private UUID id;
    private AccountStatus status;
    private String ownerName;
    private String ownerDocument;
    private LocalDate ownerBirthDate;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;

    public Account(UUID id,
                   AccountStatus status,
                   String ownerName,
                   String ownerDocument,
                   LocalDate ownerBirthDate,
                   String phoneNumber,
                   String email,
                   LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.ownerName = ownerName;
        this.ownerDocument = ownerDocument;
        this.ownerBirthDate = ownerBirthDate;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.createdAt = createdAt;
    }

    public void validateAge(Integer minimumAge) {
        LocalDate today = LocalDate.now();
        LocalDate eighteenthBirthday = ownerBirthDate.plusYears(minimumAge);
        if (today.isBefore(eighteenthBirthday)) {
            throw new IllegalArgumentException("Account owner must be at least 18 years old.");
        }
    }

    public void activate() {
        if (this.status != AccountStatus.PENDING) {
            throw new InvalidAccountException(
                    "Only PENDING accounts can be activated. Current status: " + this.status);
        }
        this.status = AccountStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerDocument() {
        return ownerDocument;
    }

    public LocalDate getOwnerBirthDate() {
        return ownerBirthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


}


