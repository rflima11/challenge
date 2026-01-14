package br.com.itau.account_service.infra.persistence.entity;

import br.com.itau.account_service.domain.entity.Account;
import br.com.itau.account_service.domain.entity.AccountStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "tb_account")
public class AccountEntity {

    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    private String ownerName;
    @Column(unique = true)
    private String ownerDocument;
    private LocalDate ownerBirthDate;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;

    public Account toDomain() {
        return new Account(
                this.id,
                this.status,
                this.ownerName,
                this.ownerDocument,
                this.ownerBirthDate,
                this.phoneNumber,
                this.email,
                this.createdAt
        );
    }

    public static AccountEntity fromDomain(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.setId(account.getId());
        entity.setStatus(account.getStatus());
        entity.setOwnerName(account.getOwnerName());
        entity.setOwnerDocument(account.getOwnerDocument());
        entity.setOwnerBirthDate(account.getOwnerBirthDate());
        entity.setPhoneNumber(account.getPhoneNumber());
        entity.setEmail(account.getEmail());
        entity.setCreatedAt(account.getCreatedAt());
        return entity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerDocument() {
        return ownerDocument;
    }

    public void setOwnerDocument(String ownerDocument) {
        this.ownerDocument = ownerDocument;
    }

    public LocalDate getOwnerBirthDate() {
        return ownerBirthDate;
    }

    public void setOwnerBirthDate(LocalDate ownerBirthDate) {
        this.ownerBirthDate = ownerBirthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}