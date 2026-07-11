package com.stockmate.service;

import com.stockmate.dto.CalculatorForm;
import com.stockmate.dto.CalculatorResult;
import com.stockmate.dto.TrancheForm;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceTest {

    private final CalculatorService service = new CalculatorService();

    @Test
    void testCalculateByLotsWithFeesAndLaddering() {
        CalculatorForm form = new CalculatorForm();
        form.setCurrentLots(10);
        form.setCurrentAvgPrice(BigDecimal.valueOf(1000));
        form.setCalculationMode("LOT");
        form.setBuyFeePercent(BigDecimal.valueOf(0.15)); // 0.15%
        form.setSellFeePercent(BigDecimal.valueOf(0.25)); // 0.25%
        form.setTargetSellPrice(BigDecimal.valueOf(1200));

        TrancheForm t1 = new TrancheForm(BigDecimal.valueOf(900), 5, null);
        TrancheForm t2 = new TrancheForm(BigDecimal.valueOf(800), 5, null);
        form.setTranches(List.of(t1, t2));

        CalculatorResult result = service.calculate(form);

        // Assertions for main average down
        assertEquals(0, result.newAvgPrice().compareTo(BigDecimal.valueOf(925.64)));
        assertEquals(20, result.totalLots());
        assertEquals(0, result.capitalSpentGross().compareTo(BigDecimal.valueOf(850000)));
        assertEquals(0, result.capitalSpentNet().compareTo(BigDecimal.valueOf(851275)));
        assertEquals(0, result.totalCapitalGross().compareTo(BigDecimal.valueOf(1850000)));
        assertEquals(0, result.totalCapitalNet().compareTo(BigDecimal.valueOf(1851275)));
        assertEquals(10, result.lotsBought());

        // Assertions for Take Profit Simulator
        assertEquals(0, result.grossReturn().compareTo(BigDecimal.valueOf(2400000)));
        assertEquals(0, result.sellFee().compareTo(BigDecimal.valueOf(6000)));
        assertEquals(0, result.netReturn().compareTo(BigDecimal.valueOf(2394000)));
        assertEquals(0, result.netProfit().compareTo(BigDecimal.valueOf(542725)));
        assertEquals(0, result.profitPercentage().compareTo(BigDecimal.valueOf(29.32)));
    }

    @Test
    void testCalculateByBudgetWithFees() {
        CalculatorForm form = new CalculatorForm();
        form.setCurrentLots(10);
        form.setCurrentAvgPrice(BigDecimal.valueOf(1000));
        form.setCalculationMode("BUDGET");
        form.setBuyFeePercent(BigDecimal.valueOf(0.15)); // 0.15%
        
        // Budget = 500,000. Buy Price = 900.
        // Price per lot = 90,000. Price per lot net = 90,000 * 1.0015 = 90,135
        // Lots to buy = floor(500,000 / 90,135) = 5 lots
        // Spent Net = 5 * 90,135 = 450,675
        // Leftover = 500,000 - 450,675 = 49,325
        TrancheForm t = new TrancheForm(BigDecimal.valueOf(900), null, BigDecimal.valueOf(500000));
        form.setTranches(List.of(t));

        CalculatorResult result = service.calculate(form);

        assertEquals(15, result.totalLots());
        assertEquals(5, result.lotsBought());
        assertEquals(0, result.capitalSpentNet().compareTo(BigDecimal.valueOf(450675)));
        assertEquals(0, result.leftoverBudget().compareTo(BigDecimal.valueOf(49325)));
    }
}
