package com.stockmate.controller;

import com.stockmate.model.Portfolio;
import com.stockmate.model.PortfolioItem;
import com.stockmate.model.User;
import com.stockmate.repository.PortfolioItemRepository;
import com.stockmate.repository.PortfolioRepository;
import com.stockmate.service.UserService;
import com.stockmate.service.StockPriceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.math.RoundingMode;

@Controller
public class DashboardController {

    private final UserService userService;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final StockPriceService stockPriceService;

    public DashboardController(
            UserService userService,
            PortfolioRepository portfolioRepository,
            PortfolioItemRepository portfolioItemRepository,
            StockPriceService stockPriceService
    ) {
        this.userService = userService;
        this.portfolioRepository = portfolioRepository;
        this.portfolioItemRepository = portfolioItemRepository;
        this.stockPriceService = stockPriceService;
    }

    public static class CombinedItem {
        private String stockCode;
        private int totalLots;
        private BigDecimal averagePrice;

        public CombinedItem(String stockCode, int lots, BigDecimal avgPrice) {
            this.stockCode = stockCode;
            this.totalLots = lots;
            this.averagePrice = avgPrice;
        }

        public void add(int lots, BigDecimal avgPrice) {
            int newTotalLots = this.totalLots + lots;
            if (newTotalLots > 0) {
                BigDecimal totalValA = BigDecimal.valueOf(this.totalLots).multiply(this.averagePrice);
                BigDecimal totalValB = BigDecimal.valueOf(lots).multiply(avgPrice);
                this.averagePrice = totalValA.add(totalValB).divide(BigDecimal.valueOf(newTotalLots), 4, RoundingMode.HALF_UP);
            }
            this.totalLots = newTotalLots;
        }

        public String getStockCode() { return stockCode; }
        public int getTotalLots() { return totalLots; }
        public BigDecimal getAveragePrice() { return averagePrice; }
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        List<Portfolio> portfolios = portfolioRepository.findByUser(user);
        
        // Calculate consolidated portfolio summary
        Map<String, CombinedItem> summaryMap = new HashMap<>();
        for (Portfolio p : portfolios) {
            for (PortfolioItem item : p.getItems()) {
                String code = item.getStockCode().toUpperCase();
                CombinedItem combined = summaryMap.get(code);
                if (combined == null) {
                    combined = new CombinedItem(code, item.getCurrentLots(), item.getCurrentAvgPrice());
                    summaryMap.put(code, combined);
                } else {
                    combined.add(item.getCurrentLots(), item.getCurrentAvgPrice());
                }
            }
        }
        
        model.addAttribute("portfolios", portfolios);
        model.addAttribute("summaryItems", new ArrayList<>(summaryMap.values()));
        model.addAttribute("username", user.getUsername());
        return "dashboard";
    }

    @PostMapping("/portfolio/create")
    public String createPortfolio(@RequestParam("name") String name, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Nama portfolio tidak boleh kosong");
            return "redirect:/dashboard";
        }
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        portfolioRepository.save(new Portfolio(name.trim(), user));
        redirectAttributes.addFlashAttribute("successMsg", "Portfolio '" + name + "' berhasil dibuat!");
        return "redirect:/dashboard";
    }

    @PostMapping("/portfolio/delete/{id}")
    public String deletePortfolio(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        Portfolio portfolio = portfolioRepository.findById(id).orElse(null);
        if (portfolio != null && portfolio.getUser().getUsername().equals(principal.getName())) {
            portfolioRepository.delete(portfolio);
            redirectAttributes.addFlashAttribute("successMsg", "Portfolio berhasil dihapus!");
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/portfolio/item/delete/{itemId}")
    public String deletePortfolioItem(@PathVariable Long itemId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        PortfolioItem item = portfolioItemRepository.findById(itemId).orElse(null);
        if (item != null && item.getPortfolio().getUser().getUsername().equals(principal.getName())) {
            portfolioItemRepository.delete(item);
            redirectAttributes.addFlashAttribute("successMsg", "Saham berhasil dihapus dari portfolio!");
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/portfolio/{portfolioId}/item/add")
    public String addPortfolioItem(
            @PathVariable Long portfolioId,
            @RequestParam("stockCode") String stockCode,
            @RequestParam("currentLots") Integer currentLots,
            @RequestParam("currentAvgPrice") BigDecimal currentAvgPrice,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null) {
            return "redirect:/login";
        }
        if (stockCode == null || stockCode.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Kode saham tidak boleh kosong");
            return "redirect:/dashboard";
        }
        if (currentLots == null || currentLots < 0) {
            redirectAttributes.addFlashAttribute("errorMsg", "Jumlah lot tidak valid");
            return "redirect:/dashboard";
        }
        if (currentAvgPrice == null || currentAvgPrice.compareTo(BigDecimal.ZERO) < 0) {
            redirectAttributes.addFlashAttribute("errorMsg", "Harga rata-rata tidak valid");
            return "redirect:/dashboard";
        }

        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
        if (portfolio == null || !portfolio.getUser().getUsername().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Portfolio tidak ditemukan atau tidak valid");
            return "redirect:/dashboard";
        }

        String normalizedCode = stockPriceService.normalizeTicker(stockCode.trim());

        PortfolioItem item = portfolio.getItems().stream()
                .filter(i -> i.getStockCode().equalsIgnoreCase(normalizedCode))
                .findFirst()
                .orElseGet(() -> {
                    PortfolioItem newItem = new PortfolioItem();
                    newItem.setStockCode(normalizedCode);
                    newItem.setPortfolio(portfolio);
                    return newItem;
                });

        item.setCurrentLots(currentLots);
        item.setCurrentAvgPrice(currentAvgPrice);
        portfolioItemRepository.save(item);

        redirectAttributes.addFlashAttribute("successMsg", "Saham " + normalizedCode + " berhasil disimpan ke portfolio '" + portfolio.getName() + "'!");
        return "redirect:/dashboard";
    }

    @PostMapping("/portfolio/item/update/{itemId}")
    public String updatePortfolioItem(
            @PathVariable Long itemId,
            @RequestParam("currentLots") Integer currentLots,
            @RequestParam("currentAvgPrice") BigDecimal currentAvgPrice,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null) {
            return "redirect:/login";
        }
        if (currentLots == null || currentLots < 0) {
            redirectAttributes.addFlashAttribute("errorMsg", "Jumlah lot tidak valid");
            return "redirect:/dashboard";
        }
        if (currentAvgPrice == null || currentAvgPrice.compareTo(BigDecimal.ZERO) < 0) {
            redirectAttributes.addFlashAttribute("errorMsg", "Harga rata-rata tidak valid");
            return "redirect:/dashboard";
        }

        PortfolioItem item = portfolioItemRepository.findById(itemId).orElse(null);
        if (item != null && item.getPortfolio().getUser().getUsername().equals(principal.getName())) {
            item.setCurrentLots(currentLots);
            item.setCurrentAvgPrice(currentAvgPrice);
            portfolioItemRepository.save(item);
            redirectAttributes.addFlashAttribute("successMsg", "Saham " + item.getStockCode() + " berhasil diperbarui!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Saham tidak ditemukan atau akses ditolak");
        }
        return "redirect:/dashboard";
    }
}
