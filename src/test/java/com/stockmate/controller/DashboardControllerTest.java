package com.stockmate.controller;

import com.stockmate.model.Portfolio;
import com.stockmate.model.User;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.stockmate.config.SecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PortfolioService portfolioService;

    @MockBean
    private StockPriceService stockPriceService;

    private User testUser;
    private Portfolio testPortfolio;

    @BeforeEach
    public void setUp() {
        testUser = new User("user1", "pass");
        testUser.setId(1L);

        testPortfolio = new Portfolio("Porto 1", testUser);
        testPortfolio.setId(1L);
        testPortfolio.setItems(new ArrayList<>());
    }

    @Test
    @WithMockUser(username = "user1")
    public void testShowDashboard() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(portfolioService.getPortfoliosByUser(testUser)).thenReturn(new ArrayList<>());
        when(portfolioService.getConsolidatedSummary(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("portfolios"))
                .andExpect(model().attributeExists("summaryItems"))
                .andExpect(model().attributeExists("username"));
    }

    @Test
    public void testShowDashboardUnauthenticated() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testCreatePortfolio() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/portfolio/create")
                .with(csrf())
                .param("name", "New Portfolio"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMsg"));

        verify(portfolioService, times(1)).createPortfolio("New Portfolio", testUser);
    }
    
    @Test
    @WithMockUser(username = "user1")
    public void testCreatePortfolioEmptyName() throws Exception {
        mockMvc.perform(post("/portfolio/create")
                .with(csrf())
                .param("name", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testDeletePortfolio() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(portfolioService.deletePortfolio(1L, testUser)).thenReturn(true);

        mockMvc.perform(post("/portfolio/delete/1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testDeletePortfolioItem() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(portfolioService.deletePortfolioItem(10L, testUser)).thenReturn(true);

        mockMvc.perform(post("/portfolio/item/delete/10")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testAddPortfolioItem() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(portfolioService.getPortfolioById(1L)).thenReturn(Optional.of(testPortfolio));
        when(stockPriceService.normalizeTicker("BBCA")).thenReturn("BBCA");

        mockMvc.perform(post("/portfolio/1/item/add")
                .with(csrf())
                .param("stockCode", "BBCA")
                .param("currentLots", "10")
                .param("currentAvgPrice", "8000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMsg"));

        verify(portfolioService, times(1)).addOrUpdateItem(testPortfolio, "BBCA", 10, new BigDecimal("8000"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testDeletePortfolioFailed() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(portfolioService.deletePortfolio(2L, testUser)).thenReturn(false);

        mockMvc.perform(post("/portfolio/delete/2")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testDeletePortfolioItemFailed() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(portfolioService.deletePortfolioItem(20L, testUser)).thenReturn(false);

        mockMvc.perform(post("/portfolio/item/delete/20")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testAddPortfolioItemEmptyCode() throws Exception {
        mockMvc.perform(post("/portfolio/1/item/add")
                .with(csrf())
                .param("stockCode", "")
                .param("currentLots", "10")
                .param("currentAvgPrice", "8000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testAddPortfolioItemInvalidLots() throws Exception {
        mockMvc.perform(post("/portfolio/1/item/add")
                .with(csrf())
                .param("stockCode", "BBCA")
                .param("currentLots", "-10")
                .param("currentAvgPrice", "8000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testAddPortfolioItemInvalidPrice() throws Exception {
        mockMvc.perform(post("/portfolio/1/item/add")
                .with(csrf())
                .param("stockCode", "BBCA")
                .param("currentLots", "10")
                .param("currentAvgPrice", "-8000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testAddPortfolioItemPortfolioNotFound() throws Exception {
        when(portfolioService.getPortfolioById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/portfolio/1/item/add")
                .with(csrf())
                .param("stockCode", "BBCA")
                .param("currentLots", "10")
                .param("currentAvgPrice", "8000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testUpdatePortfolioItem() throws Exception {
        com.stockmate.model.PortfolioItem item = new com.stockmate.model.PortfolioItem();
        item.setStockCode("BBCA");
        item.setCurrentLots(5);
        item.setCurrentAvgPrice(new BigDecimal("7000"));
        item.setPortfolio(testPortfolio);

        when(portfolioService.getPortfolioItemById(10L)).thenReturn(Optional.of(item));

        mockMvc.perform(post("/portfolio/item/update/10")
                .with(csrf())
                .param("currentLots", "15")
                .param("currentAvgPrice", "7500"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMsg"));

        verify(portfolioService, times(1)).updateItem(item);
    }

    @Test
    @WithMockUser(username = "user1")
    public void testUpdatePortfolioItemInvalidLots() throws Exception {
        mockMvc.perform(post("/portfolio/item/update/10")
                .with(csrf())
                .param("currentLots", "-15")
                .param("currentAvgPrice", "7500"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testUpdatePortfolioItemInvalidPrice() throws Exception {
        mockMvc.perform(post("/portfolio/item/update/10")
                .with(csrf())
                .param("currentLots", "15")
                .param("currentAvgPrice", "-7500"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testUpdatePortfolioItemNotFound() throws Exception {
        when(portfolioService.getPortfolioItemById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/portfolio/item/update/10")
                .with(csrf())
                .param("currentLots", "15")
                .param("currentAvgPrice", "7500"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMsg"));
    }
}
