package com.stockmate.service;

import com.stockmate.dto.CalculatorForm;
import com.stockmate.dto.CalculatorResult;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CalculatorService {

    private static final BigDecimal SHARES_PER_LOT = BigDecimal.valueOf(100);

    public CalculatorResult calculate(CalculatorForm form) {
        BigDecimal currentLots = BigDecimal.valueOf(form.getCurrentLots() != null ? form.getCurrentLots() : 0);
        BigDecimal currentAvgPrice = form.getCurrentAvgPrice() != null ? form.getCurrentAvgPrice() : BigDecimal.ZERO;
        BigDecimal buyPrice = form.getBuyPrice() != null ? form.getBuyPrice() : BigDecimal.ZERO;
        String mode = form.getCalculationMode();

        BigDecimal currentShares = currentLots.multiply(SHARES_PER_LOT);
        BigDecimal currentCapital = currentShares.multiply(currentAvgPrice);

        BigDecimal lotsToBuy;
        BigDecimal capitalSpent;
        BigDecimal leftoverBudget = BigDecimal.ZERO;

        if ("BUDGET".equalsIgnoreCase(mode)) {
            BigDecimal budget = form.getTargetBudget();
            if (budget == null) {
                budget = BigDecimal.ZERO;
            }
            BigDecimal pricePerLot = buyPrice.multiply(SHARES_PER_LOT);
            if (pricePerLot.compareTo(BigDecimal.ZERO) > 0) {
                lotsToBuy = budget.divide(pricePerLot, 0, RoundingMode.FLOOR);
            } else {
                lotsToBuy = BigDecimal.ZERO;
            }
            BigDecimal sharesToBuy = lotsToBuy.multiply(SHARES_PER_LOT);
            capitalSpent = sharesToBuy.multiply(buyPrice);
            leftoverBudget = budget.subtract(capitalSpent);
        } else {
            // LOT mode
            Integer targetLotsInt = form.getTargetLots();
            lotsToBuy = targetLotsInt != null ? BigDecimal.valueOf(targetLotsInt) : BigDecimal.ZERO;
            BigDecimal sharesToBuy = lotsToBuy.multiply(SHARES_PER_LOT);
            capitalSpent = sharesToBuy.multiply(buyPrice);
        }

        BigDecimal totalLots = currentLots.add(lotsToBuy);
        BigDecimal totalShares = totalLots.multiply(SHARES_PER_LOT);
        BigDecimal totalCapital = currentCapital.add(capitalSpent);

        BigDecimal newAvgPrice = BigDecimal.ZERO;
        BigDecimal avgDropPercentage = BigDecimal.ZERO;

        if (totalShares.compareTo(BigDecimal.ZERO) > 0) {
            newAvgPrice = totalCapital.divide(totalShares, 2, RoundingMode.HALF_UP);
        }

        if (currentAvgPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal priceDifference = currentAvgPrice.subtract(newAvgPrice);
            avgDropPercentage = priceDifference.multiply(BigDecimal.valueOf(100))
                    .divide(currentAvgPrice, 2, RoundingMode.HALF_UP);
        }

        return new CalculatorResult(
                newAvgPrice,
                totalLots.intValue(),
                capitalSpent.setScale(2, RoundingMode.HALF_UP),
                totalCapital.setScale(2, RoundingMode.HALF_UP),
                lotsToBuy.intValue(),
                leftoverBudget.setScale(2, RoundingMode.HALF_UP),
                avgDropPercentage
        );
    }
}
