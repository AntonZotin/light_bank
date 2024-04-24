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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

    private final ConcurrentMap<String, ReentrantLock> locks;

    private final AccountRepository repository;

    private final TransactionService transactionService;

    public AccountServiceImpl(AccountRepository repository, TransactionService transactionService) {
        this.repository = repository;
        this.transactionService = transactionService;
        this.locks = new ConcurrentHashMap<>();
    }

    public void deposit(Account account, Double amount) {
        withLockAndTransaction(() -> {
            transactionService.deposit(account, amount);
            account.setBalance(account.getBalance() + amount);
            repository.save(account);
        }, "deposit");
    }

    public void transfer(Account account, Double amount, Account receiver) {
        withLockAndTransaction(() -> {
            if (account.getBalance() < amount) {
                throw new InsufficientFundsException();
            }
            transactionService.transfer(account, amount, receiver);
            receiver.setBalance(receiver.getBalance() + amount);
            repository.save(receiver);
            account.setBalance(account.getBalance() - amount);
            repository.save(account);
        }, "transfer");
    }

    public void withdraw(Account account, Double amount) {
        withLockAndTransaction(() -> {
            if (account.getBalance() < amount) {
                throw new InsufficientFundsException();
            }
            transactionService.withdraw(account, amount);
            account.setBalance(account.getBalance() - amount);
            repository.save(account);
        }, "withdraw");
    }

    public void undoTransaction(Account account, Long transactionId) {
        withLockAndTransaction(() -> {
            Transaction transaction = transactionService.get(transactionId);
            if (!transaction.getAccount().getId().equals(account.getId())) {
                throw new TransactionsPermissionDeniedException();
            }
            Double amount = transaction.getAmount();
            switch (transaction.getPurpose()) {
                case (Transaction.DEPOSIT), (Transaction.WITHDRAW) -> {
                    transactionService.undo(transaction.getId());
                    account.setBalance(account.getBalance() - amount);
                    repository.save(account);
                }
                case (Transaction.TRANSFER) -> {
                    Account receiver = (transaction.getAmount() > 0) ? transaction.getSender() : transaction.getReceiver();
                    transactionService.undo(transaction.getTransferId());
                    receiver.setBalance(receiver.getBalance() + amount);
                    repository.save(receiver);
                    account.setBalance(account.getBalance() - amount);
                    repository.save(account);
                }
                default -> throw new UnknownPurposeException(transaction.getPurpose());
            }
        }, "undoTransaction");
    }

    public List<String> listAccountNames() {
        return repository.findAll().stream().map(a -> a.getUser().getUsername()).sorted().collect(Collectors.toList());
    }

    public String getUsername(String paymentAccount) {
        return repository.getByPaymentAccount(paymentAccount).getUser().getUsername();
    }

    private void withLockAndTransaction(Runnable task, String methodKey) {
        ReentrantLock lock = locks.computeIfAbsent(methodKey, k -> new ReentrantLock());
        lock.lock();
        try{
            withTransaction(task);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    void withTransaction(Runnable task) {
        task.run();
    }
}
