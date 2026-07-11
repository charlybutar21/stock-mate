package com.stockmate.controller;

import com.stockmate.dto.CalculatorForm;
import com.stockmate.dto.CalculatorResult;
import com.stockmate.model.Portfolio;
import com.stockmate.model.User;
import com.stockmate.service.CalculatorService;
import com.stockmate.service.PortfolioService;
import com.stockmate.service.StockPriceService;
import com.stockmate.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.stockmate.config.SecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(CalculatorController.class)
@Import(SecurityConfig.class)
public class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalculatorService calculatorService;

    @MockBean
    private StockPriceService stockPriceService;

    @MockBean
    private UserService userService;

    @MockBean
    private PortfolioService portfolioService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User("user1", "pass");
        testUser.setId(1L);
    }

    @Test
    @WithMockUser(username = "user1")
    public void testShowCalculator() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(portfolioService.getPortfoliosByUser(testUser)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/")
                .param("stockCode", "BBCA")
                .param("currentLots", "10")
                .param("currentAvgPrice", "8000"))
                .andExpect(status().isOk())
                .andExpect(view().name("calculator"))
                .andExpect(model().attributeExists("calculatorForm"))
                .andExpect(model().attributeExists("portfolios"));
    }

    @Test
    public void testShowCalculatorUnauthenticated() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testCalculateAverageDownValid() throws Exception {
        CalculatorResult fakeResult = CalculatorResult.builder()
                .newAvgPrice(new BigDecimal("1000"))
                .avgDropPercentage(BigDecimal.ZERO)
                .leftoverBudget(BigDecimal.ZERO)
                .build();
        when(calculatorService.calculate(any())).thenReturn(fakeResult);
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        
        mockMvc.perform(post("/")
                .with(csrf())
                .param("calculationMode", "LOT")
                .param("currentLots", "10")
                .param("currentAvgPrice", "1000")
                .param("buyFeePercent", "0.15")
                .param("sellFeePercent", "0.25")
                .param("tranches[0].buyPrice", "900")
                .param("tranches[0].targetLots", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("calculator"))
                .andExpect(model().attributeExists("result"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testCalculateAverageDownInvalid() throws Exception {
        mockMvc.perform(post("/")
                .with(csrf())
                .param("calculationMode", "LOT")
                .param("currentLots", "-10"))
                .andExpect(status().isOk())
                .andExpect(view().name("calculator"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "user1")
    public void testSaveToPortfolio() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        Portfolio portfolio = new Portfolio("Porto 1", testUser);
        when(portfolioService.getPortfoliosByUser(testUser)).thenReturn(List.of(portfolio));
        when(stockPriceService.normalizeTicker("BBCA")).thenReturn("BBCA");

        mockMvc.perform(post("/portfolio/save")
                .with(csrf())
                .param("stockCode", "BBCA")
                .param("currentLots", "10")
                .param("currentAvgPrice", "8000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMsg"));
                
        verify(portfolioService, times(1)).addOrUpdateItem(any(), eq("BBCA"), eq(10), eq(new BigDecimal("8000")));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testCalculateAverageDownInvalidTranches() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(portfolioService.getPortfoliosByUser(testUser)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/")
                .with(csrf())
                .param("calculationMode", "LOT")
                .param("currentLots", "10")
                .param("currentAvgPrice", "1000")
                .param("tranches[0].buyPrice", "0")
                .param("tranches[0].targetLots", "-5"))
                .andExpect(status().isOk())
                .andExpect(view().name("calculator"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "user1")
    public void testCalculateAverageDownInvalidTranchesBudget() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(portfolioService.getPortfoliosByUser(testUser)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/")
                .with(csrf())
                .param("calculationMode", "BUDGET")
                .param("currentLots", "10")
                .param("currentAvgPrice", "1000")
                .param("tranches[0].buyPrice", "")
                .param("tranches[0].targetBudget", "-50000"))
                .andExpect(status().isOk())
                .andExpect(view().name("calculator"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "user1")
    public void testCalculateAverageDownEmptyTranches() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(portfolioService.getPortfoliosByUser(testUser)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/")
                .with(csrf())
                .param("calculationMode", "BUDGET")
                .param("currentLots", "10")
                .param("currentAvgPrice", "1000"))
                .andExpect(status().isOk())
                .andExpect(view().name("calculator"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "user1")
    public void testSaveToPortfolioEmptyCode() throws Exception {
        mockMvc.perform(post("/portfolio/save")
                .with(csrf())
                .param("stockCode", "   ")
                .param("currentLots", "10")
                .param("currentAvgPrice", "8000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testSaveToPortfolioInvalidLots() throws Exception {
        mockMvc.perform(post("/portfolio/save")
                .with(csrf())
                .param("stockCode", "BBCA")
                .param("currentLots", "-10")
                .param("currentAvgPrice", "8000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testSaveToPortfolioInvalidAvgPrice() throws Exception {
        mockMvc.perform(post("/portfolio/save")
                .with(csrf())
                .param("stockCode", "BBCA")
                .param("currentLots", "10")
                .param("currentAvgPrice", "-8000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testSaveToPortfolioWithPortfolioId() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        Portfolio portfolio = new Portfolio("Porto 1", testUser);
        when(portfolioService.getPortfolioById(2L)).thenReturn(Optional.of(portfolio));
        when(stockPriceService.normalizeTicker("BBCA")).thenReturn("BBCA");

        mockMvc.perform(post("/portfolio/save")
                .with(csrf())
                .param("portfolioId", "2")
                .param("stockCode", "BBCA")
                .param("currentLots", "10")
                .param("currentAvgPrice", "8000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMsg"));
                
        verify(portfolioService, times(1)).addOrUpdateItem(any(), eq("BBCA"), eq(10), eq(new BigDecimal("8000")));
    }
}
