package com.stockmate.service;

import org.springframework.stereotype.Service;

@Service
public class StockPriceService {

    public String normalizeTicker(String ticker) {
        if (ticker == null) {
            return null;
        }
        return ticker.trim().toUpperCase().replace(".JK", "");
    }
}
