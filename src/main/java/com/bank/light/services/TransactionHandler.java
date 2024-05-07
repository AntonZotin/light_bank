package com.bank.light.services;

import com.bank.light.domain.Transaction;
import com.bank.light.models.NotificationMsg;
import com.bank.light.repositories.TransactionRepository;
import com.bank.light.services.rabbitmq.NotificationSender;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Service;

@Aspect
@Service
public class TransactionHandler {

    private final NotificationSender sender;

    private final TransactionRepository repository;

    public TransactionHandler(final TransactionRepository repository, NotificationSender sender) {
        this.repository = repository;
        this.sender = sender;
    }

    @Before("execution(* com.bank.light.repositories.TransactionRepository.save(..))")
    public void beforeSave(final JoinPoint jp) {
        final Object[] args = jp.getArgs();
        if (args[0] instanceof Transaction transaction) {
            final LocalDateTime now = LocalDateTime.now();
            if (transaction.getAmount() >= 10_000 || transaction.getAmount() <= -10_000) {
                pushNotification(transaction, "More than 10000");
            }
            final int hour = now.getHour();
            if (hour >= 1 && hour <= 7) {
                pushNotification(transaction, "User must sleep");
            }
            final LocalDateTime limit = now.minus(1, ChronoUnit.DAYS);
            if (repository.countByAccountAndCreatedAtAfter(transaction.getAccount(), limit) > 5) {
                pushNotification(transaction, "Too much transactions");
            }
        }
    }

    private void pushNotification(final Transaction transaction, final String message) {
        sender.enqueueNotification(new NotificationMsg(transaction.getAccount().getPaymentAccount(), message));
    }
}
