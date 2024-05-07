package com.bank.light.services;

import com.bank.light.domain.Account;
import com.bank.light.domain.Transaction;
import com.bank.light.exceptions.InsufficientFundsException;
import com.bank.light.exceptions.TransactionsPermissionDeniedException;
import com.bank.light.exceptions.UnknownPurposeException;
import com.bank.light.interfaces.AccountService;
import com.bank.light.interfaces.TransactionService;
import com.bank.light.interfaces.UserService;
import com.bank.light.repositories.AccountRepository;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

    private final ConcurrentMap<String, ReentrantLock> locks;

    private final AccountRepository repository;

    private final TransactionService transactionService;

    private final UserService userService;

    public AccountServiceImpl(final AccountRepository repository, final TransactionService transactionService, UserService userService) {
        this.userService = userService;
        this.locks = new ConcurrentHashMap<>();
        this.repository = repository;
        this.transactionService = transactionService;
    }

    public void deposit(final String username, final Double amount) {
        withLockAndTransaction(() -> {
            final Account account = userService.getAccountByUsername(username);
            transactionService.deposit(account, amount);
            account.setBalance(account.getBalance() + amount);
            repository.save(account);
        }, username);
    }

    public void transfer(final String username, final Double amount, final String receiverUsername) {
        List<String> usernames = Stream.of(username, receiverUsername).sorted().map(Object::toString).toList();
        withLockAndTransaction(() -> withLockAndTransaction(() -> {
            final Account account = userService.getAccountByUsername(username);
            final Account receiver = userService.getAccountByUsername(receiverUsername);
            if (account.getBalance() < amount) {
                throw new InsufficientFundsException();
            }
            transactionService.transfer(account, amount, receiver);
            receiver.setBalance(receiver.getBalance() + amount);
            repository.save(receiver);
            account.setBalance(account.getBalance() - amount);
            repository.save(account);
        }, usernames.get(1)), usernames.get(0));
    }

    public void withdraw(final String username, final Double amount) {
        withLockAndTransaction(() -> {
            final Account account = userService.getAccountByUsername(username);
            if (account.getBalance() < amount) {
                throw new InsufficientFundsException();
            }
            transactionService.withdraw(account, amount);
            account.setBalance(account.getBalance() - amount);
            repository.save(account);
        }, username);
    }

    public void undoTransaction(final String username, final Long transactionId) {
        withLockAndTransaction(() -> {
            final Account account = userService.getAccountByUsername(username);
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
        }, username);
    }

    public List<String> listAccountNames() {
        return repository.findAll().stream().map(a -> a.getUser().getUsername()).sorted().toList();
    }

    public String getUsername(final String paymentAccount) {
        return repository.getByPaymentAccount(paymentAccount).getUser().getUsername();
    }

    @Transactional
    void withLockAndTransaction(final Runnable task, final String username) {
        final ReentrantLock lock = locks.computeIfAbsent(username, k -> new ReentrantLock());
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }
}
