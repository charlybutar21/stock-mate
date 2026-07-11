package com.stockmate.controller;

import com.stockmate.dto.CalculatorForm;
import com.stockmate.dto.CalculatorResult;
import com.stockmate.dto.TrancheForm;
import com.stockmate.model.Portfolio;
import com.stockmate.model.PortfolioItem;
import com.stockmate.model.User;
import com.stockmate.repository.PortfolioItemRepository;
import com.stockmate.repository.PortfolioRepository;
import com.stockmate.service.CalculatorService;
import com.stockmate.service.StockPriceService;
import com.stockmate.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CalculatorController {

    private final CalculatorService calculatorService;
    private final StockPriceService stockPriceService;
    private final UserService userService;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    public CalculatorController(
            CalculatorService calculatorService,
            StockPriceService stockPriceService,
            UserService userService,
            PortfolioRepository portfolioRepository,
            PortfolioItemRepository portfolioItemRepository
    ) {
        this.calculatorService = calculatorService;
        this.stockPriceService = stockPriceService;
        this.userService = userService;
        this.portfolioRepository = portfolioRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    @GetMapping("/")
    public String showCalculator(
            @RequestParam(value = "stockCode", required = false) String stockCode,
            @RequestParam(value = "currentLots", required = false) Integer currentLots,
            @RequestParam(value = "currentAvgPrice", required = false) BigDecimal currentAvgPrice,
            Principal principal,
            Model model
    ) {
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
                model.addAttribute("portfolios", portfolioRepository.findByUser(user));
            }
        }
        return "calculator";
    }

    @PostMapping("/")
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
                    model.addAttribute("portfolios", portfolioRepository.findByUser(user));
                }
            }
            return "calculator";
        }

        CalculatorResult result = calculatorService.calculate(form);
        model.addAttribute("result", result);
        
        if (principal != null) {
            User user = userService.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("portfolios", portfolioRepository.findByUser(user));
            }
        }
        return "calculator";
    }

    @PostMapping("/portfolio/save")
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
            portfolio = portfolioRepository.findById(portfolioId)
                    .filter(p -> p.getUser().getId().equals(user.getId()))
                    .orElseGet(() -> portfolioRepository.save(new Portfolio("Portfolio Utama", user)));
        } else {
            // Get or create first portfolio
            portfolio = portfolioRepository.findByUser(user).stream()
                    .findFirst()
                    .orElseGet(() -> portfolioRepository.save(new Portfolio("Portfolio Utama", user)));
        }

        String normalizedCode = stockPriceService.normalizeTicker(form.getStockCode());

        // Find existing stock in portfolio or create new
        PortfolioItem item = portfolio.getItems().stream()
                .filter(i -> i.getStockCode().equalsIgnoreCase(normalizedCode))
                .findFirst()
                .orElseGet(() -> {
                    PortfolioItem newItem = new PortfolioItem();
                    newItem.setStockCode(normalizedCode);
                    newItem.setPortfolio(portfolio);
                    return newItem;
                });

        item.setCurrentLots(form.getCurrentLots());
        item.setCurrentAvgPrice(form.getCurrentAvgPrice());
        portfolioItemRepository.save(item);

        redirectAttributes.addFlashAttribute("successMsg", "Saham " + normalizedCode + " berhasil disimpan ke portfolio '" + portfolio.getName() + "'!");
        return "redirect:/dashboard";
    }

    @GetMapping("/api/stock/price")
    @ResponseBody
    public ResponseEntity<?> getStockPrice(@RequestParam("ticker") String ticker) {
        BigDecimal price = stockPriceService.fetchCurrentPrice(ticker);
        String normalized = stockPriceService.normalizeTicker(ticker);
        Map<String, Object> response = new HashMap<>();
        response.put("ticker", normalized);
        response.put("price", price);
        return ResponseEntity.ok(response);
    }
}
