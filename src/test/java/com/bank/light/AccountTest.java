package com.bank.light;

import com.bank.light.config.AbstractTest;
import com.bank.light.domain.Account;
import com.bank.light.domain.Transaction;
import com.bank.light.domain.User;
import com.bank.light.exceptions.BalanceNotNullException;
import com.bank.light.exceptions.UserNotFoundException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AccountTest extends AbstractTest {

    @Test
    void registerUserTest() {
        User user = registerUser();
        Assertions.assertNotNull(user);
        boolean match = Pattern.matches("[\\w\\d]{8}-[\\w\\d]{4}-[\\w\\d]{4}-[\\w\\d]{4}-[\\w\\d]{12}", user.getAccount().getPaymentAccount());
        Assertions.assertTrue(match);
        Assertions.assertEquals(0.0, user.getAccount().getBalance());
    }

    @Test
    void balanceTest() {
        User user = registerUser();
        accountService.deposit(user.getAccount(), 3.0);
        List<Transaction> history = transactionService.findByAccount(user.getAccount());
        Assertions.assertEquals(1, history.size());
        Assertions.assertEquals(3.0, history.get(0).getAmount());
        Assertions.assertEquals(3.0, user.getAccount().getBalance());
        accountService.withdraw(user.getAccount(), 1.0);
        history = transactionService.findByAccount(user.getAccount());
        Assertions.assertEquals(2, history.size());
        Assertions.assertEquals(-1.0, history.get(1).getAmount());
        Assertions.assertEquals(2.0, user.getAccount().getBalance());
        Assertions.assertThrows(BalanceNotNullException.class, () -> userService.disableUser(user.getUsername()));
        accountService.withdraw(user.getAccount(), 2.0);
        history = transactionService.findByAccount(user.getAccount());
        Assertions.assertEquals(3, history.size());
        Assertions.assertEquals(-2.0, history.get(2).getAmount());
        Assertions.assertEquals(0.0, user.getAccount().getBalance());
        userService.disableUser(user.getUsername());
        Assertions.assertThrows(UserNotFoundException.class, () -> userService.getAccountByUsername(user.getUsername()));
    }

    @Test
    void transferTest() throws InterruptedException {
        int amount = 1_000;
        User user = registerUser();
        User user2 = registerUser("testUser2");
        User user3 = registerUser("testUser3");
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.submit(depositAndTransfer(user.getAccount(), user.getUsername(), user2.getAccount(), amount));
        executorService.submit(depositAndTransfer(user2.getAccount(), user2.getUsername(), user3.getAccount(), amount));
        executorService.submit(depositAndTransfer(user3.getAccount(), user3.getUsername(), user.getAccount(), amount));
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
        Account account = userService.getAccountByUsername(user.getUsername());
        Assertions.assertEquals(amount, account.getBalance());
        Assertions.assertEquals(amount * 2 + 1, transactionService.findByAccount(account).size());
        Account account2 = userService.getAccountByUsername(user2.getUsername());
        Assertions.assertEquals(amount, account2.getBalance());
        Assertions.assertEquals(amount * 2 + 1, transactionService.findByAccount(account2).size());
        Account account3 = userService.getAccountByUsername(user3.getUsername());
        Assertions.assertEquals(amount, account3.getBalance());
        Assertions.assertEquals(amount * 2 + 1, transactionService.findByAccount(account3).size());
    }

    @Test
    void undoTransactionTest() {
        User user = registerUser();
        User user2 = registerUser("testUser2");
        accountService.deposit(user.getAccount(), 100.0);
        accountService.transfer(user.getAccount(), 10.0, user2.getAccount());
        accountService.transfer(user.getAccount(), 15.0, user2.getAccount());
        accountService.transfer(user.getAccount(), 20.0, user2.getAccount());
        Assertions.assertEquals(55, user.getAccount().getBalance());
        Assertions.assertEquals(45, user2.getAccount().getBalance());
        Transaction transaction = transactionService.findByAccount(user.getAccount()).get(1);
        Assertions.assertEquals(-10, transaction.getAmount());
        accountService.undoTransaction(user.getAccount(), transaction.getId());
        Assertions.assertEquals(65, userService.getAccountByUsername(user.getUsername()).getBalance());
        Account account2 = userService.getAccountByUsername(user2.getUsername());
        Assertions.assertEquals(35, account2.getBalance());
        transaction = transactionService.findByAccount(user2.getAccount()).get(0);
        Assertions.assertEquals(15, transaction.getAmount());
        accountService.undoTransaction(account2, transaction.getId());
        Assertions.assertEquals(80, userService.getAccountByUsername(user.getUsername()).getBalance());
        Assertions.assertEquals(20, userService.getAccountByUsername(user2.getUsername()).getBalance());
    }

    @Test
    void lockTest() throws InterruptedException {
        int amount = 10_000;
        User user = registerUser();
        User user2 = registerUser("testUser2");
        accountService.deposit(user.getAccount(), (double) amount);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(deposit(user.getAccount(), user.getUsername(), amount));
        executorService.submit(transfer(user.getAccount(), user.getUsername(), user2.getAccount(), amount));
        executorService.shutdown();
        executorService.awaitTermination(120, TimeUnit.SECONDS);
        Account account = userService.getAccountByUsername(user.getUsername());
        Assertions.assertEquals(amount, account.getBalance());
        Assertions.assertEquals(amount * 2 + 1, transactionService.findByAccount(account).size());
    }
}
