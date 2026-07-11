package com.stockmate.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StockPriceServiceTest {

    private final StockPriceService stockPriceService = new StockPriceService();

    @Test
    public void testNormalizeTicker() {
        assertEquals("BBCA", stockPriceService.normalizeTicker("BBCA.JK"));
        assertEquals("BBRI", stockPriceService.normalizeTicker("BBRI"));
        assertEquals("ANTM", stockPriceService.normalizeTicker("ANTM "));
        assertNull(stockPriceService.normalizeTicker(null));
        assertEquals("", stockPriceService.normalizeTicker(""));
    }

}
