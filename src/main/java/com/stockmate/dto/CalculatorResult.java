package com.stockmate.dto;

import java.math.BigDecimal;

public record CalculatorResult(
    BigDecimal newAvgPrice,
    Integer totalLots,
    BigDecimal capitalSpent,
    BigDecimal totalCapital,
    Integer lotsBought,
    BigDecimal leftoverBudget,
    BigDecimal avgDropPercentage
) {}
