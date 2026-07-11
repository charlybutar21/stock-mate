package com.stockmate.service;

import com.stockmate.dto.CalculatorForm;
import com.stockmate.dto.CalculatorResult;
import com.stockmate.dto.TrancheForm;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CalculatorService {

    private static final BigDecimal SHARES_PER_LOT = BigDecimal.valueOf(100);
    private static final BigDecimal PERCENT_DIVISOR = BigDecimal.valueOf(100);

    public CalculatorResult calculate(CalculatorForm form) {
        BigDecimal buyFeeRate = form.getBuyFeePercent() != null
                ? form.getBuyFeePercent().divide(PERCENT_DIVISOR, 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal sellFeeRate = form.getSellFeePercent() != null
                ? form.getSellFeePercent().divide(PERCENT_DIVISOR, 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalLotsToBuy = BigDecimal.ZERO;
        BigDecimal totalCapitalSpentGross = BigDecimal.ZERO;
        BigDecimal totalCapitalSpentNet = BigDecimal.ZERO;
        BigDecimal totalLeftoverBudget = BigDecimal.ZERO;

        if (form.getTranches() != null) {
            for (TrancheForm tranche : form.getTranches()) {
                BigDecimal buyPrice = tranche.getBuyPrice() != null ? tranche.getBuyPrice() : BigDecimal.ZERO;
                if (buyPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal pricePerLot = buyPrice.multiply(SHARES_PER_LOT);
                BigDecimal lotsToBuy = BigDecimal.ZERO;
                BigDecimal leftover = BigDecimal.ZERO;

                if ("BUDGET".equalsIgnoreCase(form.getCalculationMode())) {
                    BigDecimal budget = tranche.getTargetBudget() != null ? tranche.getTargetBudget() : BigDecimal.ZERO;
                    // cost per lot net = pricePerLot * (1 + buyFeeRate)
                    BigDecimal costPerLotNet = pricePerLot.multiply(BigDecimal.ONE.add(buyFeeRate));
                    if (costPerLotNet.compareTo(BigDecimal.ZERO) > 0) {
                        lotsToBuy = budget.divide(costPerLotNet, 0, RoundingMode.FLOOR);
                    }
                    BigDecimal sharesToBuy = lotsToBuy.multiply(SHARES_PER_LOT);
                    BigDecimal spentGross = sharesToBuy.multiply(buyPrice);
                    BigDecimal spentNet = spentGross.multiply(BigDecimal.ONE.add(buyFeeRate));
                    leftover = budget.subtract(spentNet);

                    totalLotsToBuy = totalLotsToBuy.add(lotsToBuy);
                    totalCapitalSpentGross = totalCapitalSpentGross.add(spentGross);
                    totalCapitalSpentNet = totalCapitalSpentNet.add(spentNet);
                    totalLeftoverBudget = totalLeftoverBudget.add(leftover);
                } else {
                    // LOT mode
                    BigDecimal lots = tranche.getTargetLots() != null ? BigDecimal.valueOf(tranche.getTargetLots()) : BigDecimal.ZERO;
                    BigDecimal sharesToBuy = lots.multiply(SHARES_PER_LOT);
                    BigDecimal spentGross = sharesToBuy.multiply(buyPrice);
                    BigDecimal spentNet = spentGross.multiply(BigDecimal.ONE.add(buyFeeRate));

                    totalLotsToBuy = totalLotsToBuy.add(lots);
                    totalCapitalSpentGross = totalCapitalSpentGross.add(spentGross);
                    totalCapitalSpentNet = totalCapitalSpentNet.add(spentNet);
                }
            }
        }

        BigDecimal currentLots = BigDecimal.valueOf(form.getCurrentLots() != null ? form.getCurrentLots() : 0);
        BigDecimal currentAvgPrice = form.getCurrentAvgPrice() != null ? form.getCurrentAvgPrice() : BigDecimal.ZERO;
        BigDecimal currentShares = currentLots.multiply(SHARES_PER_LOT);
        BigDecimal currentCapital = currentShares.multiply(currentAvgPrice);

        BigDecimal totalLots = currentLots.add(totalLotsToBuy);
        BigDecimal totalShares = totalLots.multiply(SHARES_PER_LOT);
        BigDecimal totalCapitalGross = currentCapital.add(totalCapitalSpentGross);
        BigDecimal totalCapitalNet = currentCapital.add(totalCapitalSpentNet);

        BigDecimal newAvgPrice = BigDecimal.ZERO;
        if (totalShares.compareTo(BigDecimal.ZERO) > 0) {
            newAvgPrice = totalCapitalNet.divide(totalShares, 2, RoundingMode.HALF_UP);
        }

        BigDecimal avgDropPercentage = BigDecimal.ZERO;
        if (currentAvgPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = currentAvgPrice.subtract(newAvgPrice);
            avgDropPercentage = diff.multiply(PERCENT_DIVISOR).divide(currentAvgPrice, 2, RoundingMode.HALF_UP);
        }

        // Take profit simulation
        BigDecimal targetSellPrice = form.getTargetSellPrice();
        BigDecimal grossReturn = null;
        BigDecimal sellFee = null;
        BigDecimal netReturn = null;
        BigDecimal netProfit = null;
        BigDecimal profitPercentage = null;

        if (targetSellPrice != null && targetSellPrice.compareTo(BigDecimal.ZERO) > 0) {
            grossReturn = totalShares.multiply(targetSellPrice);
            sellFee = grossReturn.multiply(sellFeeRate);
            netReturn = grossReturn.subtract(sellFee);
            netProfit = netReturn.subtract(totalCapitalNet);
            if (totalCapitalNet.compareTo(BigDecimal.ZERO) > 0) {
                profitPercentage = netProfit.multiply(PERCENT_DIVISOR).divide(totalCapitalNet, 2, RoundingMode.HALF_UP);
            } else {
                profitPercentage = BigDecimal.ZERO;
            }
            
            grossReturn = grossReturn.setScale(2, RoundingMode.HALF_UP);
            sellFee = sellFee.setScale(2, RoundingMode.HALF_UP);
            netReturn = netReturn.setScale(2, RoundingMode.HALF_UP);
            netProfit = netProfit.setScale(2, RoundingMode.HALF_UP);
            profitPercentage = profitPercentage.setScale(2, RoundingMode.HALF_UP);
        }

        return new CalculatorResult(
                newAvgPrice.setScale(2, RoundingMode.HALF_UP),
                totalLots.intValue(),
                totalCapitalSpentGross.setScale(2, RoundingMode.HALF_UP),
                totalCapitalSpentNet.setScale(2, RoundingMode.HALF_UP),
                totalCapitalGross.setScale(2, RoundingMode.HALF_UP),
                totalCapitalNet.setScale(2, RoundingMode.HALF_UP),
                totalLotsToBuy.intValue(),
                totalLeftoverBudget.setScale(2, RoundingMode.HALF_UP),
                avgDropPercentage.setScale(2, RoundingMode.HALF_UP),
                targetSellPrice,
                grossReturn,
                sellFee,
                netReturn,
                netProfit,
                profitPercentage
        );
    }
}
