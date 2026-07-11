package com.stockmate.service;

import org.springframework.stereotype.Service;

@Service
public class StockPriceService {

    public String normalizeTicker(String ticker) {
        if (ticker == null) {
            return null;
        }
        ticker = ticker.trim().toUpperCase();
        // If ticker is exactly 4 alphabetical letters, assume it's Indonesian IDX stock and append .JK
        if (ticker.matches("^[A-Z]{4}$")) {
            return ticker + ".JK";
        }
        return ticker;
    }
}
