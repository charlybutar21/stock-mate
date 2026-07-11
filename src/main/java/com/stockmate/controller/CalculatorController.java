package com.stockmate.controller;

import com.stockmate.dto.CalculatorForm;
import com.stockmate.dto.CalculatorResult;
import com.stockmate.service.CalculatorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping("/")
    public String showCalculator(Model model) {
        model.addAttribute("calculatorForm", new CalculatorForm());
        return "calculator";
    }

    @PostMapping("/")
    public String calculateAverageDown(
            @Valid @ModelAttribute("calculatorForm") CalculatorForm form,
            BindingResult bindingResult,
            Model model
    ) {
        // Cross-field validation
        if ("LOT".equalsIgnoreCase(form.getCalculationMode())) {
            if (form.getTargetLots() == null) {
                bindingResult.rejectValue("targetLots", "NotNull", "Jumlah lot yang ingin dibeli harus diisi");
            } else if (form.getTargetLots() <= 0) {
                bindingResult.rejectValue("targetLots", "Min", "Jumlah lot yang ingin dibeli harus minimal 1");
            }
        } else if ("BUDGET".equalsIgnoreCase(form.getCalculationMode())) {
            if (form.getTargetBudget() == null) {
                bindingResult.rejectValue("targetBudget", "NotNull", "Budget nominal dana harus diisi");
            } else if (form.getTargetBudget().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue("targetBudget", "DecimalMin", "Budget nominal dana harus lebih besar dari 0");
            }
        }

        if (bindingResult.hasErrors()) {
            return "calculator";
        }

        CalculatorResult result = calculatorService.calculate(form);
        model.addAttribute("result", result);
        return "calculator";
    }
}
