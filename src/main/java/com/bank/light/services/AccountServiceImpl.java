package com.bank.light.services;

import com.bank.light.domain.Account;
import com.bank.light.domain.Transaction;
import com.bank.light.exceptions.InsufficientFundsException;
import com.bank.light.exceptions.TransactionsPermissionDeniedException;
import com.bank.light.exceptions.UnknownPurposeException;
import com.bank.light.interfaces.AccountService;
import com.bank.light.interfaces.TransactionService;
import com.bank.light.repositories.AccountRepository;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AccountServiceImpl implements AccountService {

    private final ConcurrentMap<String, ReentrantLock> locks;

    private final AccountRepository repository;

    private final TransactionService transactionService;

    private final TransactionTemplate template;

    public AccountServiceImpl(final AccountRepository repository, final TransactionService transactionService, final TransactionTemplate template) {
        this.locks = new ConcurrentHashMap<>();
        this.repository = repository;
        this.transactionService = transactionService;
        this.template = template;
    }

    public void deposit(final Account account, final Double amount) {
        withLockAndTransaction(() -> {
            transactionService.deposit(account, amount);
            account.setBalance(account.getBalance() + amount);
            repository.save(account);
        }, account.getId().toString());
    }

    public void transfer(final Account account, final Double amount, final Account receiver) {
        String accountIds = Stream.of(account.getId(), receiver.getId()).sorted()
                .map(Object::toString).collect(Collectors.joining(","));
        withLockAndTransaction(() -> {
            if (account.getBalance() < amount) {
                throw new InsufficientFundsException();
            }
            transactionService.transfer(account, amount, receiver);
            receiver.setBalance(receiver.getBalance() + amount);
            repository.save(receiver);
            account.setBalance(account.getBalance() - amount);
            repository.save(account);
        }, accountIds);
    }

    public void withdraw(final Account account, final Double amount) {
        withLockAndTransaction(() -> {
            if (account.getBalance() < amount) {
                throw new InsufficientFundsException();
            }
            transactionService.withdraw(account, amount);
            account.setBalance(account.getBalance() - amount);
            repository.save(account);
        }, account.getId().toString());
    }

    public void undoTransaction(final Account account, final Long transactionId) {
        withLockAndTransaction(() -> {
            final Transaction transaction = transactionService.get(transactionId);
            if (!transaction.getAccount().getId().equals(account.getId())) {
                throw new TransactionsPermissionDeniedException();
            }
            final Double amount = transaction.getAmount();
            switch (transaction.getPurpose()) {
                case (Transaction.DEPOSIT), (Transaction.WITHDRAW) -> {
                    transactionService.undo(transaction.getId());
                    account.setBalance(account.getBalance() - amount);
                    repository.save(account);
                }
                case (Transaction.TRANSFER) -> {
                    final Account receiver = (transaction.getAmount() > 0) ? transaction.getSender() : transaction.getReceiver();
                    transactionService.undo(transaction.getTransferId());
                    receiver.setBalance(receiver.getBalance() + amount);
                    repository.save(receiver);
                    account.setBalance(account.getBalance() - amount);
                    repository.save(account);
                }
                default -> throw new UnknownPurposeException(transaction.getPurpose());
            }
        }, account.getId().toString());
    }

    public List<String> listAccountNames() {
        return repository.findAll().stream().map(a -> a.getUser().getUsername()).sorted().toList();
    }

    public String getUsername(final String paymentAccount) {
        return repository.getByPaymentAccount(paymentAccount).getUser().getUsername();
    }

    private void withLockAndTransaction(final Runnable task, final String accountId) {
        final ReentrantLock lock = locks.computeIfAbsent(accountId, k -> new ReentrantLock());
        lock.lock();
        try {
            template.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(final TransactionStatus status) {
                    task.run();
                }
            });
        } finally {
            lock.unlock();
        }
    }
}
