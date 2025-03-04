package com.example.backend.service;

import com.example.backend.model.Account;
import com.example.backend.model.Transaction;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
    private static final double INITIAL_BALANCE = 10000.0;

    @Getter
    private final Account account;
    private final KrakenWebsocketService krakenService;

    @Getter
    private final List<Transaction> transactionHistory = new ArrayList<>();

    //Initialize a new account
    public AccountService(KrakenWebsocketService krakenService) {
        this.krakenService = krakenService;
        account = new Account();
        account.setBalance(INITIAL_BALANCE);
    }

    //Buy functionality
    public String buyCrypto(String symbol, double quantity) {
        Double price = krakenService.getLatestPrice(symbol);
        if (price == null) {
            return "Price for " + symbol + " not available.";
        }

        double cost = price * quantity;

        if (account.getBalance() < cost) {
            return "Insufficient balance.";
        }


        //Deduct cost and update holdings
        double newBalance = round(account.getBalance() - cost, 2);
        account.setBalance(newBalance);

        double currentQuantity = account.getHoldings().getOrDefault(symbol, 0.0);
        account.getHoldings().put(symbol, currentQuantity + quantity);

        //Recording the transaction
        Transaction transaction = new Transaction(LocalDateTime.now(), symbol, "BUY",
                quantity, price, cost);
        transactionHistory.add(transaction);

        return "Bought " + quantity + " of " + symbol + " at $" + price + " per unit, total cost: $" + cost;
    }

    public String sellCrypto(String symbol, double quantity) {
        Double price = krakenService.getLatestPrice(symbol);
        if (price == null) {
            return "Price for " + symbol + " not available.";
        }

        double currentHolding = account.getHoldings().getOrDefault(symbol, 0.0);
        if (currentHolding < quantity) {
            return "Insufficient holdings.";
        }

        double revenue = price * quantity;
        double newBalance = round(account.getBalance() + revenue, 2);
        account.setBalance(newBalance);

        //Update holdings
        double remaining = currentHolding - quantity;
        if (remaining == 0) {
            account.getHoldings().remove(symbol);
        } else {
          account.getHoldings().put(symbol, remaining);
        }

        //Recording the transaction
        Transaction transaction = new Transaction(LocalDateTime.now(), symbol, "SELL",
                quantity, price, revenue);
        transactionHistory.add(transaction);

        return "Sold " + quantity + " of " + symbol + " at $" + price + " per unit, total revenue: $" + revenue;
    }

    public void resetAccount() {
        account.setBalance(INITIAL_BALANCE);
        account.getHoldings().clear();
        transactionHistory.clear();
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
