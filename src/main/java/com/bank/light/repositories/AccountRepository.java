package com.bank.light.repositories;

import com.bank.light.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account getByPaymentAccount(String paymentAccount);
}
