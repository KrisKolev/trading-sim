package com.example.backend.repository;

import com.example.backend.model.Crypto;

import java.util.ArrayList;
import java.util.List;

public class CryptoRepository {
    public List<Crypto> cryptoList = new ArrayList<>();

    public List<Crypto> getAllCrypto() {
        return cryptoList;
    }
}
