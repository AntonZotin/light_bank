package com.bank.light.services;

import com.bank.light.domain.Transaction;
import com.bank.light.interfaces.AccountService;
import com.bank.light.interfaces.NotificationService;
import com.bank.light.repositories.TransactionRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Service;

@Aspect
@Service
public class TransactionHandler {

    private final NotificationService notificationService;

    private final AccountService accountService;

    private final TransactionRepository repository;

    public TransactionHandler(final TransactionRepository repository, final NotificationService notificationService, final AccountService accountService) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.accountService = accountService;
    }

    @Before("execution(* com.bank.light.repositories.TransactionRepository.save(..))")
    public void beforeSave(final JoinPoint jp) {
        final Object[] args = jp.getArgs();
        if (args[0] instanceof Transaction transaction) {
            final LocalDateTime now = LocalDateTime.now();
            if (transaction.getAmount() >= 10_000 || transaction.getAmount() <= -10_000) {
                pushNotification(transaction.getAccount().getPaymentAccount(), "More than 10000");
            }
            final int hour = now.getHour();
            if (hour >= 1 && hour <= 7) {
                pushNotification(transaction.getAccount().getPaymentAccount(), "User must sleep");
            }
            final LocalDateTime limit = now.minus(1, ChronoUnit.DAYS);
            if (repository.countByAccountAndCreatedAtAfter(transaction.getAccount(), limit) > 5) {
                pushNotification(transaction.getAccount().getPaymentAccount(), "Too much transactions");
            }
        }
    }

    private void pushNotification(final String paymentAccount, final String message) {
        notificationService.notifyManagers(accountService.getUsername(paymentAccount), message);
    }
}
