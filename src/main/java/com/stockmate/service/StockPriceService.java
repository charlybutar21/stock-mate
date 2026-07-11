package com.stockmate.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StockPriceService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public StockPriceService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

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

    public BigDecimal fetchCurrentPrice(String ticker) {
        String normalized = normalizeTicker(ticker);
        if (normalized == null || normalized.isEmpty()) {
            return BigDecimal.ZERO;
        }

        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + normalized;
        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                return BigDecimal.ZERO;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode resultNode = root.path("chart").path("result");
            if (resultNode.isArray() && resultNode.size() > 0) {
                JsonNode meta = resultNode.get(0).path("meta");
                double price = meta.path("regularMarketPrice").asDouble();
                return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            // Log warning and fall back to zero (or handle gracefully in controller)
            System.err.println("Error fetching price for ticker " + normalized + ": " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }
}
