package com.stockmate.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "portfolio_items")
public class PortfolioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String stockCode;

    @Column(nullable = false)
    private Integer currentLots;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentAvgPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    public PortfolioItem() {}

    public PortfolioItem(String stockCode, Integer currentLots, BigDecimal currentAvgPrice, Portfolio portfolio) {
        this.stockCode = stockCode;
        this.currentLots = currentLots;
        this.currentAvgPrice = currentAvgPrice;
        this.portfolio = portfolio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode != null ? stockCode.toUpperCase() : null;
    }

    public Integer getCurrentLots() {
        return currentLots;
    }

    public void setCurrentLots(Integer currentLots) {
        this.currentLots = currentLots;
    }

    public BigDecimal getCurrentAvgPrice() {
        return currentAvgPrice;
    }

    public void setCurrentAvgPrice(BigDecimal currentAvgPrice) {
        this.currentAvgPrice = currentAvgPrice;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }
}
