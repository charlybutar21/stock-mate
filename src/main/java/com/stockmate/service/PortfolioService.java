package com.stockmate.service;

import com.stockmate.model.Portfolio;
import com.stockmate.model.PortfolioItem;
import com.stockmate.repository.PortfolioItemRepository;
import com.stockmate.repository.PortfolioRepository;
import com.stockmate.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    public static class CombinedItem {
        private String stockCode;
        private int totalLots;
        private BigDecimal averagePrice;

        public CombinedItem(String stockCode, int lots, BigDecimal avgPrice) {
            this.stockCode = stockCode;
            this.totalLots = lots;
            this.averagePrice = avgPrice;
        }

        public void add(int lots, BigDecimal avgPrice) {
            int newTotalLots = this.totalLots + lots;
            if (newTotalLots > 0) {
                BigDecimal totalValA = BigDecimal.valueOf(this.totalLots).multiply(this.averagePrice);
                BigDecimal totalValB = BigDecimal.valueOf(lots).multiply(avgPrice);
                this.averagePrice = totalValA.add(totalValB).divide(BigDecimal.valueOf(newTotalLots), 4, RoundingMode.HALF_UP);
            }
            this.totalLots = newTotalLots;
        }

        public String getStockCode() { return stockCode; }
        public int getTotalLots() { return totalLots; }
        public BigDecimal getAveragePrice() { return averagePrice; }
    }

    public List<Portfolio> getPortfoliosByUser(User user) {
        return portfolioRepository.findByUser(user);
    }

    public List<CombinedItem> getConsolidatedSummary(List<Portfolio> portfolios) {
        Map<String, CombinedItem> summaryMap = new HashMap<>();
        for (Portfolio p : portfolios) {
            for (PortfolioItem item : p.getItems()) {
                String code = item.getStockCode().toUpperCase();
                CombinedItem combined = summaryMap.get(code);
                if (combined == null) {
                    combined = new CombinedItem(code, item.getCurrentLots(), item.getCurrentAvgPrice());
                    summaryMap.put(code, combined);
                } else {
                    combined.add(item.getCurrentLots(), item.getCurrentAvgPrice());
                }
            }
        }
        return new ArrayList<>(summaryMap.values());
    }

    @Transactional
    public Portfolio createPortfolio(String name, User user) {
        return portfolioRepository.save(new Portfolio(name.trim(), user));
    }

    @Transactional
    public boolean deletePortfolio(Long id, User user) {
        Portfolio portfolio = portfolioRepository.findById(id).orElse(null);
        if (portfolio != null && portfolio.getUser().getId().equals(user.getId())) {
            portfolioRepository.delete(portfolio);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deletePortfolioItem(Long itemId, User user) {
        PortfolioItem item = portfolioItemRepository.findById(itemId).orElse(null);
        if (item != null && item.getPortfolio().getUser().getId().equals(user.getId())) {
            portfolioItemRepository.delete(item);
            return true;
        }
        return false;
    }

    @Transactional
    public PortfolioItem addOrUpdateItem(Portfolio portfolio, String normalizedCode, int lots, BigDecimal avgPrice) {
        PortfolioItem item = portfolio.getItems().stream()
                .filter(i -> i.getStockCode().equalsIgnoreCase(normalizedCode))
                .findFirst()
                .orElseGet(() -> {
                    PortfolioItem newItem = new PortfolioItem();
                    newItem.setStockCode(normalizedCode);
                    newItem.setPortfolio(portfolio);
                    return newItem;
                });
        item.setCurrentLots(lots);
        item.setCurrentAvgPrice(avgPrice);
        return portfolioItemRepository.save(item);
    }
    
    public Optional<Portfolio> getPortfolioById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId);
    }
    
    public Optional<PortfolioItem> getPortfolioItemById(Long itemId) {
        return portfolioItemRepository.findById(itemId);
    }
    
    @Transactional
    public PortfolioItem updateItem(PortfolioItem item) {
        return portfolioItemRepository.save(item);
    }
}
