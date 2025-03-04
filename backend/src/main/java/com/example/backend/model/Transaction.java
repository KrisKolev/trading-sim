package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Transaction {
    private LocalDateTime timestamp;
    private String symbol;
    private String type; //What type of transaction it was
    private double quantity;
    private double price;
    private double total;
}
