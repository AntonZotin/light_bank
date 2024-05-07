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
        String username = registerUser().getUsername();
        accountService.deposit(username, 3.0);
        Account account = userService.getAccountByUsername(username);
        List<Transaction> history = transactionService.findByAccount(account);
        Assertions.assertEquals(1, history.size());
        Assertions.assertEquals(3.0, history.get(0).getAmount());
        Assertions.assertEquals(3.0, account.getBalance());
        accountService.withdraw(username, 1.0);
        account = userService.getAccountByUsername(username);
        history = transactionService.findByAccount(account);
        Assertions.assertEquals(2, history.size());
        Assertions.assertEquals(-1.0, history.get(1).getAmount());
        Assertions.assertEquals(2.0, account.getBalance());
        Assertions.assertThrows(BalanceNotNullException.class, () -> userService.disableUser(username));
        accountService.withdraw(username, 2.0);
        account = userService.getAccountByUsername(username);
        history = transactionService.findByAccount(account);
        Assertions.assertEquals(3, history.size());
        Assertions.assertEquals(-2.0, history.get(2).getAmount());
        Assertions.assertEquals(0.0, account.getBalance());
        userService.disableUser(username);
        Assertions.assertThrows(UserNotFoundException.class, () -> userService.getAccountByUsername(username));
    }

    @Test
    void transferTest() throws InterruptedException {
        int amount = 1_000;
        User user = registerUser();
        User user2 = registerUser("testUser2");
        User user3 = registerUser("testUser3");
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.submit(depositAndTransfer(user.getUsername(), user2.getUsername(), amount));
        executorService.submit(depositAndTransfer(user2.getUsername(), user3.getUsername(), amount));
        executorService.submit(depositAndTransfer(user3.getUsername(), user.getUsername(), amount));
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
        String username = registerUser().getUsername();
        String username2 = registerUser("testUser2").getUsername();
        accountService.deposit(username, 100.0);
        accountService.transfer(username, 10.0, username2);
        accountService.transfer(username, 15.0, username2);
        accountService.transfer(username, 20.0, username2);
        Account account = userService.getAccountByUsername(username);
        Account account2 = userService.getAccountByUsername(username2);
        Assertions.assertEquals(55, account.getBalance());
        Assertions.assertEquals(45, account2.getBalance());
        Transaction transaction = transactionService.findByAccount(account).get(1);
        Assertions.assertEquals(-10, transaction.getAmount());
        accountService.undoTransaction(username, transaction.getId());
        Assertions.assertEquals(65, userService.getAccountByUsername(username).getBalance());
        account2 = userService.getAccountByUsername(username2);
        Assertions.assertEquals(35, account2.getBalance());
        transaction = transactionService.findByAccount(account2).get(0);
        Assertions.assertEquals(15, transaction.getAmount());
        accountService.undoTransaction(account2.getUser().getUsername(), transaction.getId());
        Assertions.assertEquals(80, userService.getAccountByUsername(username).getBalance());
        Assertions.assertEquals(20, userService.getAccountByUsername(username2).getBalance());
    }

    @Test
    void lockTest() throws InterruptedException {
        int amount = 10_000;
        User user = registerUser();
        User user2 = registerUser("testUser2");
        accountService.deposit(user.getUsername(), (double) amount);
        System.out.println(userService.getAccountByUsername(user.getUsername()).getBalance());
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(deposit(user.getUsername(), amount));
        executorService.submit(transfer(user.getUsername(), user2.getUsername(), amount));
        executorService.shutdown();
        executorService.awaitTermination(120, TimeUnit.SECONDS);
        Account account = userService.getAccountByUsername(user.getUsername());
        Assertions.assertEquals(amount, account.getBalance());
        Assertions.assertEquals(amount * 2 + 1, transactionService.findByAccount(account).size());
    }
}
