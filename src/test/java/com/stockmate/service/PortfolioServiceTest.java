package com.stockmate.service;

import com.stockmate.model.Portfolio;
import com.stockmate.model.PortfolioItem;
import com.stockmate.model.User;
import com.stockmate.repository.PortfolioItemRepository;
import com.stockmate.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private User user;
    private Portfolio portfolio;
    private PortfolioItem item1;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("testuser", "password");
        user.setId(1L);

        portfolio = new Portfolio("Test Portfolio", user);
        portfolio.setId(1L);
        portfolio.setItems(new ArrayList<>());

        item1 = new PortfolioItem();
        item1.setId(1L);
        item1.setStockCode("BBCA");
        item1.setCurrentLots(10);
        item1.setCurrentAvgPrice(new BigDecimal("8000"));
        item1.setPortfolio(portfolio);

        portfolio.getItems().add(item1);
    }

    @Test
    public void testGetPortfoliosByUser() {
        when(portfolioRepository.findByUser(user)).thenReturn(List.of(portfolio));
        List<Portfolio> result = portfolioService.getPortfoliosByUser(user);
        assertEquals(1, result.size());
        verify(portfolioRepository, times(1)).findByUser(user);
    }

    @Test
    public void testCreatePortfolio() {
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);
        Portfolio created = portfolioService.createPortfolio("Test Portfolio", user);
        assertNotNull(created);
        assertEquals("Test Portfolio", created.getName());
    }

    @Test
    public void testDeletePortfolio_Success() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        boolean result = portfolioService.deletePortfolio(1L, user);
        assertTrue(result);
        verify(portfolioRepository, times(1)).delete(portfolio);
    }

    @Test
    public void testDeletePortfolio_Fail_WrongUser() {
        User wrongUser = new User("wrong", "pass");
        wrongUser.setId(2L);
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        boolean result = portfolioService.deletePortfolio(1L, wrongUser);
        assertFalse(result);
        verify(portfolioRepository, never()).delete(any());
    }

    @Test
    public void testDeletePortfolioItem_Success() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(item1));
        boolean result = portfolioService.deletePortfolioItem(1L, user);
        assertTrue(result);
        verify(portfolioItemRepository, times(1)).delete(item1);
    }

    @Test
    public void testAddOrUpdateItem_AddNew() {
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenAnswer(i -> i.getArguments()[0]);
        PortfolioItem newItem = portfolioService.addOrUpdateItem(portfolio, "BBRI", 50, new BigDecimal("4000"));
        assertEquals("BBRI", newItem.getStockCode());
        assertEquals(50, newItem.getCurrentLots());
        assertEquals(new BigDecimal("4000"), newItem.getCurrentAvgPrice());
    }

    @Test
    public void testAddOrUpdateItem_UpdateExisting() {
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenAnswer(i -> i.getArguments()[0]);
        PortfolioItem updatedItem = portfolioService.addOrUpdateItem(portfolio, "BBCA", 20, new BigDecimal("8200"));
        assertEquals("BBCA", updatedItem.getStockCode());
        assertEquals(20, updatedItem.getCurrentLots());
        assertEquals(new BigDecimal("8200"), updatedItem.getCurrentAvgPrice());
    }

    @Test
    public void testGetConsolidatedSummary() {
        Portfolio p2 = new Portfolio("Another", user);
        p2.setId(2L);
        p2.setItems(new ArrayList<>());
        PortfolioItem item2 = new PortfolioItem();
        item2.setStockCode("BBCA");
        item2.setCurrentLots(20);
        item2.setCurrentAvgPrice(new BigDecimal("8600"));
        item2.setPortfolio(p2);
        p2.getItems().add(item2);

        List<PortfolioService.CombinedItem> summary = portfolioService.getConsolidatedSummary(List.of(portfolio, p2));
        assertEquals(1, summary.size());
        
        PortfolioService.CombinedItem combined = summary.get(0);
        assertEquals("BBCA", combined.getStockCode());
        assertEquals(30, combined.getTotalLots());
        // (10 * 8000 + 20 * 8600) / 30 = (80000 + 172000) / 30 = 252000 / 30 = 8400
        assertEquals(new BigDecimal("8400.0000"), combined.getAveragePrice());
    }
}
