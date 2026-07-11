package com.stockmate.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder

public record CalculatorResult(
    BigDecimal newAvgPrice,
    Integer totalLots,
    BigDecimal capitalSpentGross,
    BigDecimal capitalSpentNet,
    BigDecimal totalCapitalGross,
    BigDecimal totalCapitalNet,
    Integer lotsBought,
    BigDecimal leftoverBudget,
    BigDecimal avgDropPercentage,
    
    // Take Profit Simulator Fields (nullable if targetSellPrice not provided)
    BigDecimal targetSellPrice,
    BigDecimal grossReturn,
    BigDecimal sellFee,
    BigDecimal netReturn,
    BigDecimal netProfit,
    BigDecimal profitPercentage
) {}
