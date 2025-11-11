package com.finance_control.transactions.controller;

import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.exception.GlobalExceptionHandler;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper;

    private TransactionDTO sampleTransaction;
    private Page<TransactionDTO> samplePage;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Set up MockMvc with standalone setup, PageableHandlerMethodArgumentResolver, and GlobalExceptionHandler
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleTransaction = new TransactionDTO();
        sampleTransaction.setId(1L);
        sampleTransaction.setType(TransactionType.EXPENSE);
        sampleTransaction.setDescription("Test transaction");
        sampleTransaction.setAmount(new BigDecimal("100.00"));
        sampleTransaction.setUserId(1L);
        sampleTransaction.setCreatedAt(LocalDateTime.now());

        List<TransactionDTO> transactions = Arrays.asList(sampleTransaction);
        samplePage = new PageImpl<>(transactions, PageRequest.of(0, 20), 1);
    }

    @Test
    void getTransactions_WithValidParameters_ShouldReturnOk() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("type", "EXPENSE")
                .param("sortBy", "createdAt")
                .param("sortDirection", "desc")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].type").value("EXPENSE"))
                .andExpect(jsonPath("$.content[0].description").value("Test transaction"))
                .andExpect(jsonPath("$.content[0].amount").value(100.00))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getTransactions_WithSearchParameter_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("search", "grocery")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithDateRange_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithAmountRange_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("minAmount", "50.00")
                .param("maxAmount", "200.00")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithDefaultParameters_ShouldReturnOk() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithCategoryFilter_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("category", "Food")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithSubcategoryFilter_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("subcategory", "Groceries")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithSourceFilter_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("source", "Credit Card")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithTypeFilter_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("type", "INCOME")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithIsActiveFilter_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("isActive", "true")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithAllFiltersCombined_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("search", "grocery")
                .param("category", "Food")
                .param("subcategory", "Groceries")
                .param("source", "Credit Card")
                .param("type", "EXPENSE")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .param("minAmount", "50.00")
                .param("maxAmount", "200.00")
                .param("isActive", "true")
                .param("sortBy", "amount")
                .param("sortDirection", "desc")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithEmptyStringFilters_ShouldNotAddFilters() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("category", "")
                .param("subcategory", "   ")
                .param("source", "")
                .param("type", "")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithPartialFilters_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("category", "Food")
                .param("type", "EXPENSE")
                .param("isActive", "true")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithOnlyDateFilters_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithOnlyAmountFilters_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("minAmount", "10.00")
                .param("maxAmount", "500.00")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithNullSortBy_ShouldUseDefaultSort() throws Exception {
        when(transactionService.findAll(nullable(String.class), any(Map.class), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions/filtered")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }
}
