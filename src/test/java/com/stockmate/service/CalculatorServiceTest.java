package com.stockmate.service;

import com.stockmate.dto.CalculatorForm;
import com.stockmate.dto.CalculatorResult;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceTest {

    private final CalculatorService service = new CalculatorService();

    @Test
    void testCalculateByLots() {
        CalculatorForm form = new CalculatorForm();
        form.setCurrentLots(10);
        form.setCurrentAvgPrice(BigDecimal.valueOf(1000));
        form.setBuyPrice(BigDecimal.valueOf(900));
        form.setCalculationMode("LOT");
        form.setTargetLots(5);

        CalculatorResult result = service.calculate(form);

        assertEquals(0, result.newAvgPrice().compareTo(BigDecimal.valueOf(966.67)));
        assertEquals(15, result.totalLots());
        assertEquals(0, result.capitalSpent().compareTo(BigDecimal.valueOf(450000)));
        assertEquals(0, result.totalCapital().compareTo(BigDecimal.valueOf(1450000)));
        assertEquals(5, result.lotsBought());
        assertEquals(0, result.leftoverBudget().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.avgDropPercentage().compareTo(BigDecimal.valueOf(3.33)));
    }

    @Test
    void testCalculateByBudget() {
        CalculatorForm form = new CalculatorForm();
        form.setCurrentLots(10);
        form.setCurrentAvgPrice(BigDecimal.valueOf(1000));
        form.setBuyPrice(BigDecimal.valueOf(900));
        form.setCalculationMode("BUDGET");
        form.setTargetBudget(BigDecimal.valueOf(1000000));

        CalculatorResult result = service.calculate(form);

        assertEquals(0, result.newAvgPrice().compareTo(BigDecimal.valueOf(947.62)));
        assertEquals(21, result.totalLots());
        assertEquals(0, result.capitalSpent().compareTo(BigDecimal.valueOf(990000)));
        assertEquals(0, result.totalCapital().compareTo(BigDecimal.valueOf(1990000)));
        assertEquals(11, result.lotsBought());
        assertEquals(0, result.leftoverBudget().compareTo(BigDecimal.valueOf(10000)));
        assertEquals(0, result.avgDropPercentage().compareTo(BigDecimal.valueOf(5.24)));
    }
}
