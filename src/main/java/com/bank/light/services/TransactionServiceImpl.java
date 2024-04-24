package com.bank.light.services;

import com.bank.light.domain.Account;
import com.bank.light.domain.Transaction;
import com.bank.light.exceptions.TransactionsNotConsistentException;
import com.bank.light.exceptions.TransactionsNotFoundException;
import com.bank.light.interfaces.TransactionService;
import com.bank.light.repositories.TransactionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Integer PAGE_SIZE = 10;

    private final TransactionRepository repository;

    public TransactionServiceImpl(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction deposit(Account account, Double amount) {
        return repository.save(new Transaction(amount, account, Transaction.DEPOSIT));
    }

    public Transaction transfer(Account account, Double amount, Account receiver) {
        String transferId = UUID.randomUUID().toString();
        repository.save(new Transaction(amount, receiver, Transaction.TRANSFER, receiver, account, transferId));
        return repository.save(new Transaction(-amount, account, Transaction.TRANSFER, receiver, account, transferId));
    }

    public Transaction withdraw(Account account, Double amount) {
        return repository.save(new Transaction(-amount, account, Transaction.WITHDRAW));
    }

    public Transaction get(Long id) {
        Optional<Transaction> found = repository.findById(id);
        if (found.isEmpty()) {
            throw new TransactionsNotFoundException();
        }
        return found.get();
    }

    public List<Transaction> findByAccount(Account account) {
        return repository.findByAccount(account);
    }

    public List<Transaction> findAll(String username, String purpose) {
        if (username != null && purpose != null)
            return repository.findAllByAccount_User_UsernameAndPurpose(username, purpose);
        else if (username != null)
            return repository.findAllByAccount_User_Username(username);
        else if (purpose != null)
            return repository.findAllByPurpose(purpose);
        else
            return repository.findAll();
    }

    public List<Transaction> findAllByPage(Integer page, String username, String purpose) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id"));
        if (username != null && purpose != null)
            return repository.findAllByAccount_User_UsernameAndPurpose(username, purpose, pageRequest).getContent();
        else if (username != null)
            return repository.findAllByAccount_User_Username(username, pageRequest).getContent();
        else if (purpose != null)
            return repository.findAllByPurpose(purpose, pageRequest).getContent();
        else
            return repository.findAll(pageRequest).getContent();
    }

    public Long pageCount(String username, String purpose) {
        Long count;
        if (username != null && purpose != null)
            count = repository.countByAccount_User_UsernameAndPurpose(username, purpose);
        else if (username != null)
            count = repository.countByAccount_User_Username(username);
        else if (purpose != null)
            count = repository.countByPurpose(purpose);
        else
            count = repository.count();
        return Math.round(Math.ceil(count / (PAGE_SIZE * 1.0)));
    }

    public void undo(Long id) {
        repository.delete(get(id));
    }

    public void undo(String transferId) {
        List<Transaction> list = repository.findByTransferId(transferId);
        if (list.size() != 2) {
            throw new TransactionsNotConsistentException();
        }
        repository.deleteAll(list);
    }
}
