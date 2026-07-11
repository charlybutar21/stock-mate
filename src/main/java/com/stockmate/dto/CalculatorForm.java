package com.stockmate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CalculatorForm {

    @NotNull(message = "Jumlah lot saat ini tidak boleh kosong")
    @Min(value = 0, message = "Jumlah lot saat ini minimal 0")
    private Integer currentLots = 0;

    @NotNull(message = "Harga rata-rata saat ini tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Harga rata-rata saat ini tidak boleh negatif")
    private BigDecimal currentAvgPrice = BigDecimal.ZERO;

    @NotNull(message = "Harga beli baru tidak boleh kosong")
    @DecimalMin(value = "0.01", inclusive = true, message = "Harga beli baru harus lebih besar dari 0")
    private BigDecimal buyPrice;

    @NotNull(message = "Mode kalkulasi tidak boleh kosong")
    private String calculationMode = "LOT"; // "LOT" or "BUDGET"

    private Integer targetLots;

    private BigDecimal targetBudget;

    // Getters and Setters
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

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public String getCalculationMode() {
        return calculationMode;
    }

    public void setCalculationMode(String calculationMode) {
        this.calculationMode = calculationMode;
    }

    public Integer getTargetLots() {
        return targetLots;
    }

    public void setTargetLots(Integer targetLots) {
        this.targetLots = targetLots;
    }

    public BigDecimal getTargetBudget() {
        return targetBudget;
    }

    public void setTargetBudget(BigDecimal targetBudget) {
        this.targetBudget = targetBudget;
    }
}
