package com.bank.light.rest;

import com.bank.light.domain.Account;
import com.bank.light.domain.User;
import com.bank.light.exceptions.ApiException;
import com.bank.light.exceptions.BalanceNotNullException;
import com.bank.light.exceptions.InsufficientFundsException;
import com.bank.light.exceptions.TransactionsNotConsistentException;
import com.bank.light.exceptions.TransactionsNotFoundException;
import com.bank.light.exceptions.UserNotFoundException;
import com.bank.light.interfaces.AccountService;
import com.bank.light.interfaces.UserService;
import com.bank.light.models.DepositDto;
import com.bank.light.models.TransferDto;
import com.bank.light.models.UndoDto;
import com.bank.light.models.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
class ApiController {

    private final UserService userService;

    private final AccountService accountService;

    ApiController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping("/registration")
    public User registration(@RequestBody @Valid UserDto userDto) {
        return userService.register(userDto);
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Double balance(HttpServletRequest request) {
        return userService.getAccountByUsername(request.getUserPrincipal().getName()).getBalance();
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> deposit(@RequestBody @Valid DepositDto depositDto, HttpServletRequest request) {
        Account account = userService.getAccountByUsername(request.getUserPrincipal().getName());
        accountService.deposit(account, Double.parseDouble(depositDto.getAmount()));
        return SUCCESS;
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> transfer(@RequestBody @Valid TransferDto transferDto, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        if (username.equals(transferDto.getUsername())) {
            throw new ApiException("You can't transfer money to yourself.");
        } else {
            try {
                Account account = userService.getAccountByUsername(username);
                Account receiver = userService.getAccountByUsername(transferDto.getUsername());
                accountService.transfer(account, Double.parseDouble(transferDto.getAmount()), receiver);
                return SUCCESS;
            } catch (InsufficientFundsException | UserNotFoundException e) {
                throw new ApiException(e.getMessage());
            }
        }
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> withdraw(@RequestBody @Valid DepositDto depositDto, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        try {
            Account account = userService.getAccountByUsername(username);
            accountService.withdraw(account, Double.parseDouble(depositDto.getAmount()));
            return SUCCESS;
        } catch (InsufficientFundsException e) {
            throw new ApiException(e.getMessage());
        }
    }

    @PostMapping("/undo")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> undoTransaction(@RequestBody @Valid UndoDto undoDto, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        try {
            Account account = userService.getAccountByUsername(username);
            accountService.undoTransaction(account, undoDto.getTransactionId());
            return SUCCESS;
        } catch (TransactionsNotConsistentException | TransactionsNotFoundException e) {
            throw new ApiException(e.getMessage());
        }
    }

    @PostMapping("/close")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> close(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        try {
            userService.disableUser(username);
            return SUCCESS;
        } catch (UserNotFoundException | BalanceNotNullException e) {
            throw new ApiException(e.getMessage());
        }
    }

    private static final ResponseEntity<String> SUCCESS = new ResponseEntity<>("Success", HttpStatus.OK);
}