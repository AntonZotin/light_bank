package com.bank.light.controllers;

import com.bank.light.domain.Account;
import com.bank.light.domain.Transaction;
import com.bank.light.exceptions.BalanceNotNullException;
import com.bank.light.exceptions.InsufficientFundsException;
import com.bank.light.exceptions.TransactionsNotConsistentException;
import com.bank.light.exceptions.TransactionsNotFoundException;
import com.bank.light.exceptions.UserNotFoundException;
import com.bank.light.interfaces.AccountService;
import com.bank.light.interfaces.TransactionService;
import com.bank.light.interfaces.UserService;
import com.bank.light.models.DepositDto;
import com.bank.light.models.TransferDto;
import com.bank.light.models.UndoDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import static com.bank.light.domain.Transaction.SELECT_PURPOSE;

@Controller
@RequestMapping
public class UserController {

    private final UserService userService;

    private final AccountService accountService;

    private final TransactionService transactionService;

    public UserController(final UserService userService, final AccountService accountService, final TransactionService transactionService) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @GetMapping("/balance")
    public String balancePage(Principal principal, Model model) {
        final Account account = userService.getAccountByUsername(principal.getName());
        model.addAttribute("number", account.getPaymentAccount());
        model.addAttribute("balance", account.getBalance());
        return "balance";
    }

    @GetMapping("/history")
    public String historyPage(
            Principal principal,
            Model model,
            @RequestParam("purpose") Optional<String> purpose,
            @RequestParam("page") Optional<Integer> page
    ) {
        final String currentPurpose = purpose.orElse(null);
        final String account = userService.getAccountByUsername(principal.getName()).getUser().getUsername();
        final Long pages = transactionService.pageCount(account, currentPurpose);
        model.addAttribute("pages", pages);
        int currentPage = page.orElse(1) - 1;
        if (currentPage >= pages || currentPage < 0) currentPage = 0;
        final List<Transaction> transactions = transactionService.findAllByPage(currentPage, account, currentPurpose);
        model.addAttribute("transactions", transactions);
        model.addAttribute("page", currentPage);
        model.addAttribute("purpose", currentPurpose);
        model.addAttribute("purposes", List.of(SELECT_PURPOSE, Transaction.DEPOSIT, Transaction.TRANSFER, Transaction.WITHDRAW));
        return "history";
    }

    @PostMapping("/deposit")
    public String deposit(@Valid DepositDto depositDto, BindingResult result, Principal principal, RedirectAttributes redirectAttributes) {
        checkErrors(result, redirectAttributes, () -> {
            accountService.deposit(principal.getName(), Double.parseDouble(depositDto.getAmount()));
            redirectAttributes.addFlashAttribute("MSG_SUCCESS", "Deposit was successfully");
        });
        return "redirect:/balance";
    }

    @PostMapping("/transfer")
    public String transfer(@Valid TransferDto transferDto, BindingResult result, Principal principal, RedirectAttributes redirectAttributes) {
        checkErrors(result, redirectAttributes, () -> {
            if (principal.getName().equals(transferDto.getUsername())) {
                redirectAttributes.addFlashAttribute("MSG_ERROR", "You can't transfer money to yourself.");
            } else {
                try {
                    accountService.transfer(principal.getName(), Double.parseDouble(transferDto.getAmount()), transferDto.getUsername());
                    redirectAttributes.addFlashAttribute("MSG_SUCCESS", "Transfer was successfully");
                } catch (InsufficientFundsException | UserNotFoundException e) {
                    redirectAttributes.addFlashAttribute("MSG_ERROR", e.getMessage());
                }
            }
        });
        return "redirect:/balance";
    }

    @PostMapping("/withdraw")
    public String withdraw(@Valid DepositDto depositDto, BindingResult result, Principal principal, RedirectAttributes redirectAttributes) {
        checkErrors(result, redirectAttributes, () -> {
            try {
                accountService.withdraw(principal.getName(), Double.parseDouble(depositDto.getAmount()));
                redirectAttributes.addFlashAttribute("MSG_SUCCESS", "Withdraw was successfully");
            } catch (InsufficientFundsException e) {
                redirectAttributes.addFlashAttribute("MSG_ERROR", e.getMessage());
            }
        });
        return "redirect:/balance";
    }

    @PostMapping("/undo")
    public String undoTransaction(@Valid UndoDto undoDto, BindingResult result, Principal principal, RedirectAttributes redirectAttributes) {
        checkErrors(result, redirectAttributes, () -> {
            try {
                accountService.undoTransaction(principal.getName(), undoDto.getTransactionId());
                redirectAttributes.addFlashAttribute("MSG_SUCCESS", "Transaction undo successfully");
            } catch (TransactionsNotConsistentException | TransactionsNotFoundException e) {
                redirectAttributes.addFlashAttribute("MSG_ERROR", e.getMessage());
            }
        });
        return "redirect:/history";
    }

    @PostMapping("/close")
    public String close(Principal principal, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            userService.disableUser(principal.getName());
            request.logout();
            redirectAttributes.addFlashAttribute("MSG_SUCCESS", "Account closed successfully");
            return "redirect:/";
        } catch (UserNotFoundException | BalanceNotNullException | ServletException e) {
            redirectAttributes.addFlashAttribute("MSG_ERROR", e.getMessage());
            return "redirect:/balance";
        }
    }

    private void checkErrors(BindingResult result, RedirectAttributes redirectAttributes, Runnable task) {
        if(result.hasErrors()){
            String errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(", "));
            redirectAttributes.addFlashAttribute("MSG_ERROR", errors);
        } else {
            task.run();
        }
    }
}
