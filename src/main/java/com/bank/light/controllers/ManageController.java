package com.bank.light.controllers;

import com.bank.light.domain.Transaction;
import com.bank.light.interfaces.AccountService;
import com.bank.light.interfaces.TransactionService;
import com.bank.light.models.DownloadDto;
import com.bank.light.services.ExcelExporter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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


import static com.bank.light.utils.Constant.ACCOUNT;
import static com.bank.light.utils.Constant.PURPOSE;

@Controller
@RequestMapping
@PreAuthorize("hasAuthority('ROLE_MANAGER')")
public class ManageController {

    private final TransactionService transactionService;

    private final AccountService accountService;

    public ManageController(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @GetMapping("/manage")
    public String managePage(
            Model model,
            @RequestParam("account") Optional<String> account,
            @RequestParam("purpose") Optional<String> purpose,
            @RequestParam("page") Optional<Integer> page
    ) {
        String currentAccount = account.orElse(null);
        String currentPurpose = purpose.orElse(null);
        int currentPage = page.orElse(1) - 1;
        Long pages = transactionService.pageCount(currentAccount, currentPurpose);
        model.addAttribute("pages", pages);
        if (currentPage >= pages || currentPage < 0) currentPage = 0;
        List<Transaction> transactions = transactionService.findAllByPage(currentPage, currentAccount, currentPurpose);
        List<String> accounts = new ArrayList<>() {{
            add(ACCOUNT);
            addAll(accountService.listAccountNames());
        }};
        model.addAttribute("transactions", transactions);
        model.addAttribute("page", currentPage);
        model.addAttribute("account", currentAccount);
        model.addAttribute("purpose", currentPurpose);
        model.addAttribute("accounts", accounts);
        model.addAttribute("purposes", List.of(PURPOSE, Transaction.DEPOSIT, Transaction.TRANSFER, Transaction.WITHDRAW));
        return "manage";
    }

    @PostMapping("/download")
    public @ResponseBody ResponseEntity<byte[]> download(DownloadDto downloadDto) {
        List<Transaction> transactions = transactionService.findAll(downloadDto.getAccount(), downloadDto.getPurpose());
        Collections.sort(transactions);
        return ExcelExporter.export(transactions);
    }
}
