package com.bank.light.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Account implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double balance;

    @Column(nullable = false, unique = true)
    private String paymentAccount;

    @JsonIgnore
    @OneToOne(mappedBy = "account")
    private User user;

    public Account() {
        this.balance = 0.0;
        paymentAccount = UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", balance=" + balance +
                ", paymentAccount='" + paymentAccount + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id)
                && Objects.equals(balance, account.balance)
                && Objects.equals(paymentAccount, account.paymentAccount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, balance, paymentAccount);
    }
}
