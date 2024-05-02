package com.bank.light.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Transaction implements Serializable, Comparable<Transaction> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @ManyToOne
    private Account account;

    @Column(nullable = false)
    private String purpose;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    private Account receiver;

    @ManyToOne
    private Account sender;

    private String transferId;

    public Transaction(Double amount, Account account, String purpose) {
        this.amount = amount;
        this.account = account;
        this.purpose = purpose;
    }

    public Transaction(Double amount, Account account, String purpose, Account receiver, Account sender, String transferId) {
        this.amount = amount;
        this.account = account;
        this.purpose = purpose;
        this.receiver = receiver;
        this.sender = sender;
        this.transferId = transferId;
    }

    @Override
    public int compareTo(Transaction o) {
        return (int) (this.id - o.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id)
                && Objects.equals(amount, that.amount)
                && Objects.equals(purpose, that.purpose)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(transferId, that.transferId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, purpose, createdAt, transferId);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", account=" + getAccountUsername() +
                ", purpose='" + purpose + '\'' +
                ", createdAt=" + createdAt +
                ", receiver=" + getReceiverUsername() +
                ", sender=" + getSenderUsername() +
                ", transferId='" + transferId + '\'' +
                '}';
    }

    public String getAccountUsername() {
        return (this.account != null) ? this.account.getUser().getUsername() : "";
    }

    public String getReceiverUsername() {
        return (this.receiver != null) ? this.receiver.getUser().getUsername() : "";
    }

    public String getSenderUsername() {
        return (this.receiver != null) ? this.sender.getUser().getUsername() : "";
    }

    public String getCreatedAtFormatted() {
        return this.createdAt.format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy"));
    }

    public static final String DEPOSIT = "Deposit";
    public static final String WITHDRAW = "Withdraw";
    public static final String TRANSFER = "Transfer";

    public static final String SELECT_PURPOSE = "Select purpose";
}
