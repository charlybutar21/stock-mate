package com.stockmate.controller;

import com.stockmate.model.Portfolio;
import com.stockmate.model.PortfolioItem;
import com.stockmate.model.User;
import com.stockmate.service.PortfolioService;
import com.stockmate.service.UserService;
import com.stockmate.service.StockPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for user dashboard and portfolio management")
public class DashboardController {

    private final UserService userService;
    private final PortfolioService portfolioService;
    private final StockPriceService stockPriceService;

    @GetMapping("/dashboard")
    @Operation(summary = "Show Dashboard", description = "Displays user portfolios and consolidated summary")
    public String showDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        List<Portfolio> portfolios = portfolioService.getPortfoliosByUser(user);
        
        List<PortfolioService.CombinedItem> summaryItems = portfolioService.getConsolidatedSummary(portfolios);
        
        model.addAttribute("portfolios", portfolios);
        model.addAttribute("summaryItems", summaryItems);
        model.addAttribute("username", user.getUsername());
        return "dashboard";
    }

    @PostMapping("/portfolio/create")
    @Operation(summary = "Create Portfolio", description = "Creates a new portfolio for the user")
    public String createPortfolio(@RequestParam("name") String name, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Nama portfolio tidak boleh kosong");
            return "redirect:/dashboard";
        }
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        portfolioService.createPortfolio(name, user);
        redirectAttributes.addFlashAttribute("successMsg", "Portfolio '" + name + "' berhasil dibuat!");
        return "redirect:/dashboard";
    }

    @PostMapping("/portfolio/delete/{id}")
    @Operation(summary = "Delete Portfolio", description = "Deletes an existing portfolio if owned by the user")
    public String deletePortfolio(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        boolean deleted = portfolioService.deletePortfolio(id, user);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMsg", "Portfolio berhasil dihapus!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Portfolio tidak ditemukan atau akses ditolak");
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/portfolio/item/delete/{itemId}")
    @Operation(summary = "Delete Portfolio Item", description = "Deletes a stock from a portfolio")
    public String deletePortfolioItem(@PathVariable Long itemId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        boolean deleted = portfolioService.deletePortfolioItem(itemId, user);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMsg", "Saham berhasil dihapus dari portfolio!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Saham tidak ditemukan atau akses ditolak");
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/portfolio/{portfolioId}/item/add")
    @Operation(summary = "Add Portfolio Item", description = "Adds a new stock to a portfolio or updates existing one")
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

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId).orElse(null);
        if (portfolio == null || !portfolio.getUser().getUsername().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Portfolio tidak ditemukan atau tidak valid");
            return "redirect:/dashboard";
        }

        String normalizedCode = stockPriceService.normalizeTicker(stockCode.trim());
        portfolioService.addOrUpdateItem(portfolio, normalizedCode, currentLots, currentAvgPrice);

        redirectAttributes.addFlashAttribute("successMsg", "Saham " + normalizedCode + " berhasil disimpan ke portfolio '" + portfolio.getName() + "'!");
        return "redirect:/dashboard";
    }

    @PostMapping("/portfolio/item/update/{itemId}")
    @Operation(summary = "Update Portfolio Item", description = "Updates lots and average price for a stock")
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

        PortfolioItem item = portfolioService.getPortfolioItemById(itemId).orElse(null);
        if (item != null && item.getPortfolio().getUser().getUsername().equals(principal.getName())) {
            item.setCurrentLots(currentLots);
            item.setCurrentAvgPrice(currentAvgPrice);
            portfolioService.updateItem(item);
            redirectAttributes.addFlashAttribute("successMsg", "Saham " + item.getStockCode() + " berhasil diperbarui!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Saham tidak ditemukan atau akses ditolak");
        }
        return "redirect:/dashboard";
    }
}
