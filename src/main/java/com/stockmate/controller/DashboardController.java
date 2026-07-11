package com.stockmate.controller;

import com.stockmate.model.Portfolio;
import com.stockmate.model.PortfolioItem;
import com.stockmate.model.User;
import com.stockmate.repository.PortfolioItemRepository;
import com.stockmate.repository.PortfolioRepository;
import com.stockmate.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.List;

@Controller
public class DashboardController {

    private final UserService userService;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    public DashboardController(UserService userService, PortfolioRepository portfolioRepository, PortfolioItemRepository portfolioItemRepository) {
        this.userService = userService;
        this.portfolioRepository = portfolioRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        List<Portfolio> portfolios = portfolioRepository.findByUser(user);
        model.addAttribute("portfolios", portfolios);
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
}
