package com.stockmate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TrancheForm {

    @NotNull(message = "Harga beli tranche tidak boleh kosong")
    @DecimalMin(value = "0.01", inclusive = true, message = "Harga beli harus lebih besar dari 0")
    private BigDecimal buyPrice;

    @Min(value = 1, message = "Jumlah lot minimal 1")
    private Integer targetLots;

    @DecimalMin(value = "0.01", inclusive = true, message = "Target budget harus lebih besar dari 0")
    private BigDecimal targetBudget;

    public TrancheForm() {}

    public TrancheForm(BigDecimal buyPrice, Integer targetLots, BigDecimal targetBudget) {
        this.buyPrice = buyPrice;
        this.targetLots = targetLots;
        this.targetBudget = targetBudget;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
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
