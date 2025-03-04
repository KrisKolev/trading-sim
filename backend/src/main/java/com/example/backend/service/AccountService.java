package com.example.backend.service;

import com.example.backend.model.Account;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AccountService {
    private static final double INITIAL_BALANCE = 10000.0;
    private final Account account;
    private final KrakenWebsocketService krakenService;

    //Initialize a new account
    public AccountService(KrakenWebsocketService krakenService) {
        this.krakenService = krakenService;
        account = new Account();
        account.setBalance(INITIAL_BALANCE);
    }

    public Account getAccount() {
        return account;
    }

    //Buy functionality
    public String buyCrypto(String symbol, double quantity) {
        Double price = krakenService.getLatestPrice(symbol);
        if (price == null) {
            return "Price for " + symbol + " not available.";
        }

        BigDecimal bdPrice = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
        BigDecimal bdQuantity = BigDecimal.valueOf(quantity).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cost = bdPrice.multiply(bdQuantity).setScale(2, RoundingMode.HALF_UP);

        if (account.getBalance() < cost.doubleValue()) {
            return "Insufficient balance.";
        }
        account.setBalance(account.getBalance() - cost.doubleValue());
        account.getHoldings().put(symbol, account.getHoldings().getOrDefault(symbol, 0.0) + quantity);

        return "Bought " + bdQuantity + " of " + symbol + " at $" + bdPrice + " per unit, total cost: $" + cost;
    }

    public String sellCrypto(String symbol, double quantity) {
        Double price = krakenService.getLatestPrice(symbol);
        if (price == null) {
            return "Price for " + symbol + " not available.";
        }

        Double holding = account.getHoldings().get(symbol);
        if (holding == null || holding < quantity) {
            return "Insufficient holdings.";
        }

        BigDecimal bdPrice = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
        BigDecimal bdQuantity = BigDecimal.valueOf(quantity).setScale(2, RoundingMode.HALF_UP);
        BigDecimal revenue = bdPrice.multiply(bdQuantity).setScale(2, RoundingMode.HALF_UP);

        account.getHoldings().put(symbol, holding - quantity);
        account.setBalance(account.getBalance() + revenue.doubleValue());

        return "Sold " + bdQuantity + " of " + symbol + " at $" + bdPrice + " per unit, total revenue: $" + revenue;
    }

    public void resetAccount() {
        account.setBalance(INITIAL_BALANCE);
        account.getHoldings().clear();
    }
}
