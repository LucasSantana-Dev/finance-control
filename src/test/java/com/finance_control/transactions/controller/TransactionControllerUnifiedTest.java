package com.finance_control.transactions.controller;

import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransactionDTO sampleTransaction;
    private Page<TransactionDTO> samplePage;

    @BeforeEach
    void setUp() {
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
        when(transactionService.findAll(anyString(), anyString(), anyString(), any(Pageable.class), any(TransactionDTO.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions")
                .param("userId", "1")
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
        when(transactionService.findAll(anyString(), anyString(), anyString(), any(Pageable.class), any(TransactionDTO.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions")
                .param("userId", "1")
                .param("search", "grocery")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithDateRange_ShouldReturnFilteredResults() throws Exception {
        when(transactionService.findAll(anyString(), anyString(), anyString(), any(Pageable.class), any(TransactionDTO.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions")
                .param("userId", "1")
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
        when(transactionService.findAll(anyString(), anyString(), anyString(), any(Pageable.class), any(TransactionDTO.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions")
                .param("userId", "1")
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
        when(transactionService.findAll(anyString(), anyString(), anyString(), any(Pageable.class), any(TransactionDTO.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transactions")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactions_WithCategoriesData_ShouldReturnOk() throws Exception {
        when(transactionService.getCategoriesByUserId(anyLong()))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/transactions")
                .param("userId", "1")
                .param("data", "categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getTransactions_WithSubcategoriesData_ShouldReturnOk() throws Exception {
        when(transactionService.getSubcategoriesByCategoryId(anyLong()))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/transactions")
                .param("userId", "1")
                .param("data", "subcategories")
                .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getTransactions_WithSubcategoriesDataWithoutCategoryId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/transactions")
                .param("userId", "1")
                .param("data", "subcategories"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactions_WithTypesData_ShouldReturnOk() throws Exception {
        when(transactionService.getTransactionTypes())
                .thenReturn(Arrays.asList("INCOME", "EXPENSE"));

        mockMvc.perform(get("/transactions")
                .param("data", "types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("INCOME"))
                .andExpect(jsonPath("$[1]").value("EXPENSE"));
    }

    @Test
    void getTransactions_WithTotalAmountData_ShouldReturnOk() throws Exception {
        when(transactionService.getTotalAmountByUserId(anyLong()))
                .thenReturn(new BigDecimal("1000.00"));

        mockMvc.perform(get("/transactions")
                .param("userId", "1")
                .param("data", "total-amount"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(1000.00));
    }

    @Test
    void getTransactions_WithMonthlySummaryData_ShouldReturnOk() throws Exception {
        when(transactionService.getMonthlySummary(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(java.util.Map.of("2024-01", java.util.Map.of("income", 1000, "expense", 500)));

        mockMvc.perform(get("/transactions")
                .param("userId", "1")
                .param("data", "monthly-summary")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getTransactions_WithMonthlySummaryDataWithoutDates_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/transactions")
                .param("userId", "1")
                .param("data", "monthly-summary"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactions_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/transactions")
                .param("userId", "1")
                .param("data", "invalid-type"))
                .andExpect(status().isBadRequest());
    }
}
