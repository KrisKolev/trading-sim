package com.example.backend.service;

import org.springframework.stereotype.Service;

@Service
public class CryptoService {
    public double calculateTotalCost(double price, double amount) {
        return price * amount;
    }
}
