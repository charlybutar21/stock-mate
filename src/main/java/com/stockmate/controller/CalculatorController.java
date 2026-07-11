package com.stockmate.controller;

import com.stockmate.dto.CalculatorForm;
import com.stockmate.dto.CalculatorResult;
import com.stockmate.dto.TrancheForm;
import com.stockmate.model.Portfolio;
import com.stockmate.model.PortfolioItem;
import com.stockmate.model.User;
import com.stockmate.service.CalculatorService;
import com.stockmate.service.PortfolioService;
import com.stockmate.service.StockPriceService;
import com.stockmate.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Tag(name = "Calculator", description = "Endpoints for averaging down and target sell simulation")
public class CalculatorController {

    private final CalculatorService calculatorService;
    private final StockPriceService stockPriceService;
    private final UserService userService;
    private final PortfolioService portfolioService;

    @GetMapping("/")
    @Operation(summary = "Show Calculator", description = "Displays the calculator page")
    public String showCalculator(
            @RequestParam(value = "stockCode", required = false) String stockCode,
            @RequestParam(value = "currentLots", required = false) Integer currentLots,
            @RequestParam(value = "currentAvgPrice", required = false) BigDecimal currentAvgPrice,
            Principal principal,
            Model model
    ) {
        if (principal == null) {
            return "redirect:/login";
        }
        CalculatorForm form = new CalculatorForm();
        if (stockCode != null) {
            form.setStockCode(stockCode);
        }
        if (currentLots != null) {
            form.setCurrentLots(currentLots);
        }
        if (currentAvgPrice != null) {
            form.setCurrentAvgPrice(currentAvgPrice);
        }
        model.addAttribute("calculatorForm", form);

        if (principal != null) {
            User user = userService.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("portfolios", portfolioService.getPortfoliosByUser(user));
            }
        }
        return "calculator";
    }

    @PostMapping("/")
    @Operation(summary = "Calculate Average Down", description = "Performs calculation and displays results")
    public String calculateAverageDown(
            @Valid @ModelAttribute("calculatorForm") CalculatorForm form,
            BindingResult bindingResult,
            Principal principal,
            Model model
    ) {
        // Cross-field validation for multi-step tranches
        if (form.getTranches() == null || form.getTranches().isEmpty()) {
            bindingResult.rejectValue("tranches", "NotEmpty", "Minimal harus ada 1 tranche pembelian");
        } else {
            for (int i = 0; i < form.getTranches().size(); i++) {
                TrancheForm tranche = form.getTranches().get(i);
                
                if (tranche.getBuyPrice() == null) {
                    bindingResult.rejectValue("tranches[" + i + "].buyPrice", "NotNull", "Harga beli tidak boleh kosong");
                } else if (tranche.getBuyPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    bindingResult.rejectValue("tranches[" + i + "].buyPrice", "DecimalMin", "Harga beli harus lebih besar dari 0");
                }

                if ("LOT".equalsIgnoreCase(form.getCalculationMode())) {
                    if (tranche.getTargetLots() == null) {
                        bindingResult.rejectValue("tranches[" + i + "].targetLots", "NotNull", "Jumlah lot harus diisi");
                    } else if (tranche.getTargetLots() <= 0) {
                        bindingResult.rejectValue("tranches[" + i + "].targetLots", "Min", "Jumlah lot harus minimal 1");
                    }
                } else if ("BUDGET".equalsIgnoreCase(form.getCalculationMode())) {
                    if (tranche.getTargetBudget() == null) {
                        bindingResult.rejectValue("tranches[" + i + "].targetBudget", "NotNull", "Nominal budget harus diisi");
                    } else if (tranche.getTargetBudget().compareTo(BigDecimal.ZERO) <= 0) {
                        bindingResult.rejectValue("tranches[" + i + "].targetBudget", "DecimalMin", "Nominal budget harus lebih besar dari 0");
                    }
                }
            }
        }

        if (bindingResult.hasErrors()) {
            if (principal != null) {
                User user = userService.findByUsername(principal.getName()).orElse(null);
                if (user != null) {
                    model.addAttribute("portfolios", portfolioService.getPortfoliosByUser(user));
                }
            }
            return "calculator";
        }

        CalculatorResult result = calculatorService.calculate(form);
        model.addAttribute("result", result);
        
        if (principal != null) {
            User user = userService.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("portfolios", portfolioService.getPortfoliosByUser(user));
            }
        }
        return "calculator";
    }

    @PostMapping("/portfolio/save")
    @Operation(summary = "Save Result to Portfolio", description = "Saves the calculator stock inputs to user portfolio")
    public String saveToPortfolio(
            @ModelAttribute CalculatorForm form,
            @RequestParam(value = "portfolioId", required = false) Long portfolioId,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null) {
            return "redirect:/login";
        }
        if (form.getStockCode() == null || form.getStockCode().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Kode saham tidak boleh kosong untuk disimpan");
            return "redirect:/";
        }
        if (form.getCurrentLots() == null || form.getCurrentLots() < 0) {
            redirectAttributes.addFlashAttribute("errorMsg", "Jumlah lot tidak valid");
            return "redirect:/";
        }
        if (form.getCurrentAvgPrice() == null || form.getCurrentAvgPrice().compareTo(BigDecimal.ZERO) < 0) {
            redirectAttributes.addFlashAttribute("errorMsg", "Harga rata-rata tidak valid");
            return "redirect:/";
        }

        String username = principal.getName();
        User user = userService.findByUsername(username).orElseThrow();

        Portfolio portfolio;
        if (portfolioId != null) {
            portfolio = portfolioService.getPortfolioById(portfolioId)
                    .filter(p -> p.getUser().getId().equals(user.getId()))
                    .orElseGet(() -> portfolioService.createPortfolio("Portfolio Utama", user));
        } else {
            // Get or create first portfolio
            portfolio = portfolioService.getPortfoliosByUser(user).stream()
                    .findFirst()
                    .orElseGet(() -> portfolioService.createPortfolio("Portfolio Utama", user));
        }

        String normalizedCode = stockPriceService.normalizeTicker(form.getStockCode());

        portfolioService.addOrUpdateItem(portfolio, normalizedCode, form.getCurrentLots(), form.getCurrentAvgPrice());

        redirectAttributes.addFlashAttribute("successMsg", "Saham " + normalizedCode + " berhasil disimpan ke portfolio '" + portfolio.getName() + "'!");
        return "redirect:/dashboard";
    }
}
