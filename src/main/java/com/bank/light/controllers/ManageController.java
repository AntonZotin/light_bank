package com.bank.light.controllers;

import com.bank.light.domain.Account;
import com.bank.light.domain.Transaction;
import com.bank.light.interfaces.AccountService;
import com.bank.light.interfaces.TransactionService;
import com.bank.light.models.DownloadDto;
import com.bank.light.services.ExcelExporter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping
@PreAuthorize("hasAuthority('ROLE_MANAGER')")
public class ManageController {

    private final TransactionService transactionService;

    private final AccountService accountService;

    public ManageController(final TransactionService transactionService, final AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @GetMapping("/manage")
    public String managePage(Model model, @RequestParam("account") Optional<String> account, @RequestParam("purpose") Optional<String> purpose, @RequestParam("page") Optional<Integer> page) {
        final String currentAccount = account.orElse(null);
        final String currentPurpose = purpose.orElse(null);
        final Long pages = transactionService.pageCount(currentAccount, currentPurpose);
        model.addAttribute("pages", pages);
        int currentPage = page.orElse(1) - 1;
        if (currentPage >= pages || currentPage < 0) currentPage = 0;
        final List<Transaction> transactions = transactionService.findAllByPage(currentPage, currentAccount, currentPurpose);
        final List<String> accounts = new ArrayList<>() {{
            add(Account.SELECT_ACCOUNT);
            addAll(accountService.listAccountNames());
        }};
        model.addAttribute("transactions", transactions);
        model.addAttribute("page", currentPage);
        model.addAttribute("account", currentAccount);
        model.addAttribute("purpose", currentPurpose);
        model.addAttribute("accounts", accounts);
        model.addAttribute("purposes", List.of(Transaction.SELECT_PURPOSE, Transaction.DEPOSIT, Transaction.TRANSFER, Transaction.WITHDRAW));
        return "manage";
    }

    @PostMapping("/download")
    public @ResponseBody ResponseEntity<byte[]> download(DownloadDto downloadDto) {
        final List<Transaction> transactions = transactionService.findAll(downloadDto.getAccount(), downloadDto.getPurpose());
        Collections.sort(transactions);
        return ExcelExporter.export(transactions);
    }
}
