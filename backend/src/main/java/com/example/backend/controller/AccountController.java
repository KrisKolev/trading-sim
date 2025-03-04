package com.example.backend.controller;

import com.example.backend.model.Account;
import com.example.backend.model.Transaction;
import com.example.backend.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Account getAccount() {
        return accountService.getAccount();
    }

    @PostMapping("/buy")
    public String buy(@RequestParam String symbol,
                      @RequestParam double quantity) {
        return accountService.buyCrypto(symbol, quantity);
    }

    @PostMapping("/sell")
    public String sell(@RequestParam String symbol,
                       @RequestParam double quantity) {
        return accountService.sellCrypto(symbol, quantity);
    }

    @PostMapping("/reset")
    public String reset() {
        accountService.resetAccount();
        return "Account is reset!";
    }

    @GetMapping("/history")
    public List<Transaction> transactionHistory() {
        return accountService.getTransactionHistory();
    }
}
