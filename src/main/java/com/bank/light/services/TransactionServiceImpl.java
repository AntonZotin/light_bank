package com.bank.light.services;

import com.bank.light.domain.Account;
import com.bank.light.domain.Transaction;
import com.bank.light.exceptions.TransactionsNotConsistentException;
import com.bank.light.exceptions.TransactionsNotFoundException;
import com.bank.light.interfaces.TransactionService;
import com.bank.light.repositories.TransactionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Integer PAGE_SIZE = 10;

    private final TransactionRepository repository;

    public TransactionServiceImpl(final TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction deposit(final Account account, final Double amount) {
        return repository.save(new Transaction(amount, account, Transaction.DEPOSIT));
    }

    public Transaction transfer(final Account account, final Double amount, final Account receiver) {
        final String transferId = UUID.randomUUID().toString();
        repository.save(new Transaction(amount, receiver, Transaction.TRANSFER, receiver, account, transferId));
        return repository.save(new Transaction(-amount, account, Transaction.TRANSFER, receiver, account, transferId));
    }

    public Transaction withdraw(final Account account, final Double amount) {
        return repository.save(new Transaction(-amount, account, Transaction.WITHDRAW));
    }

    public Transaction get(final Long id) {
        return repository.findById(id).orElseThrow(TransactionsNotFoundException::new);
    }

    public List<Transaction> findByAccount(final Account account) {
        return repository.findByAccount(account);
    }

    public List<Transaction> findAll(final String username, final String purpose) {
        if (username != null && purpose != null)
            return repository.findAllByAccount_User_UsernameAndPurpose(username, purpose);
        else if (username != null) return repository.findAllByAccount_User_Username(username);
        else if (purpose != null) return repository.findAllByPurpose(purpose);
        else return repository.findAll();
    }

    public List<Transaction> findAllByPage(final Integer page, final String username, final String purpose) {
        final PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id"));
        if (username != null && purpose != null)
            return repository.findAllByAccount_User_UsernameAndPurpose(username, purpose, pageRequest).getContent();
        else if (username != null) return repository.findAllByAccount_User_Username(username, pageRequest).getContent();
        else if (purpose != null) return repository.findAllByPurpose(purpose, pageRequest).getContent();
        else return repository.findAll(pageRequest).getContent();
    }

    public Long pageCount(final String username, final String purpose) {
        if (username != null && purpose != null)
            return pages(repository.countByAccount_User_UsernameAndPurpose(username, purpose));
        else if (username != null) return pages(repository.countByAccount_User_Username(username));
        else if (purpose != null) return pages(repository.countByPurpose(purpose));
        else return pages(repository.count());
    }

    public void undo(final Long id) {
        repository.delete(get(id));
    }

    public void undo(final String transferId) {
        final List<Transaction> list = repository.findByTransferId(transferId);
        if (list.size() != 2) {
            throw new TransactionsNotConsistentException();
        }
        repository.deleteAll(list);
    }

    private Long pages(final Long count) {
        return Math.round(Math.ceil(count / (PAGE_SIZE * 1.0)));
    }
}
