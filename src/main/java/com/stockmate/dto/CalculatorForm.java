package com.stockmate.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CalculatorForm {

    private String stockCode;

    @NotNull(message = "Jumlah lot saat ini tidak boleh kosong")
    @Min(value = 0, message = "Jumlah lot saat ini minimal 0")
    private Integer currentLots = 0;

    @NotNull(message = "Harga rata-rata saat ini tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Harga rata-rata saat ini tidak boleh negatif")
    private BigDecimal currentAvgPrice = BigDecimal.ZERO;

    @NotNull(message = "Mode kalkulasi tidak boleh kosong")
    private String calculationMode = "LOT"; // "LOT" or "BUDGET"

    @Valid
    private List<TrancheForm> tranches = new ArrayList<>();

    @NotNull(message = "Fee beli tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee beli tidak boleh negatif")
    private BigDecimal buyFeePercent = BigDecimal.valueOf(0.15); // Pre-fill 0.15%

    @NotNull(message = "Fee jual tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee jual tidak boleh negatif")
    private BigDecimal sellFeePercent = BigDecimal.valueOf(0.25); // Pre-fill 0.25%

    @DecimalMin(value = "0.0", inclusive = true, message = "Harga jual target tidak boleh negatif")
    private BigDecimal targetSellPrice;

    public CalculatorForm() {
        // Initialize with at least one tranche
        this.tranches.add(new TrancheForm());
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
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

    public String getCalculationMode() {
        return calculationMode;
    }

    public void setCalculationMode(String calculationMode) {
        this.calculationMode = calculationMode;
    }

    public List<TrancheForm> getTranches() {
        return tranches;
    }

    public void setTranches(List<TrancheForm> tranches) {
        this.tranches = tranches;
    }

    public BigDecimal getBuyFeePercent() {
        return buyFeePercent;
    }

    public void setBuyFeePercent(BigDecimal buyFeePercent) {
        this.buyFeePercent = buyFeePercent;
    }

    public BigDecimal getSellFeePercent() {
        return sellFeePercent;
    }

    public void setSellFeePercent(BigDecimal sellFeePercent) {
        this.sellFeePercent = sellFeePercent;
    }

    public BigDecimal getTargetSellPrice() {
        return targetSellPrice;
    }

    public void setTargetSellPrice(BigDecimal targetSellPrice) {
        this.targetSellPrice = targetSellPrice;
    }
}
