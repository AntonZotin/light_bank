package com.bank.light.config;

import com.bank.light.domain.Account;
import com.bank.light.domain.User;
import com.bank.light.interfaces.AccountService;
import com.bank.light.interfaces.TransactionService;
import com.bank.light.interfaces.UserService;
import com.bank.light.models.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles(TestProfile.PROFILE_NAME)
@SpringBootTest
public abstract class AbstractTest {

    @Autowired
    public UserService userService;

    @Autowired
    public AccountService accountService;

    @Autowired
    public TransactionService transactionService;

    public User registerUser() {
        return userService.register(new UserDto("testUser", "pass", "pass"));
    }

    public User registerUser(String username) {
        return userService.register(new UserDto(username, "pass", "pass"));
    }

    public Runnable depositAndTransfer(Account account, String username, Account receiver, int amount) {
        return () -> {
            accountService.deposit(account, (double) amount);
            System.out.println(username + " deposit");
            for (int i = 1; i <= amount; i++) {
                accountService.transfer(account, 1.0, receiver);
                if (i % 100 == 0) System.out.println(username + " send " + i + "transactions.");
            }
        };
    }
}
