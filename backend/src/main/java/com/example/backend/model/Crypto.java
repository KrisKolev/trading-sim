package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Crypto {
    private String name;
    private String symbol;
    private double price;
}
