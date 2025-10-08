package com.finance_control.unit.transactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.transactions.controller.TransactionController;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.service.TransactionService;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @WebMvcTest controller layer test for TransactionController.
 * Tests REST API endpoints with mocked service layer.
 *
 * This is the industry standard approach for testing controllers in Spring Boot.
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private TransactionDTO testTransactionDTO;

    @BeforeEach
    void setUp() {
        testTransactionDTO = new TransactionDTO();
        testTransactionDTO.setId(1L);
        testTransactionDTO.setDescription("Test Transaction");
        testTransactionDTO.setAmount(BigDecimal.valueOf(100.00));
        testTransactionDTO.setType(TransactionType.INCOME);
        testTransactionDTO.setSubtype(TransactionSubtype.FIXED);
        testTransactionDTO.setSource(TransactionSource.CASH);
        testTransactionDTO.setCategoryId(1L);
        testTransactionDTO.setUserId(1L);
        testTransactionDTO.setDate(LocalDateTime.now());

        // Add responsibilities
        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setId(1L);
        responsible.setName("Test Responsible");
        responsible.setResponsibleId(1L);
        responsible.setPercentage(BigDecimal.valueOf(100.00));
        responsibilities.add(responsible);
        testTransactionDTO.setResponsibilities(responsibilities);
    }

    @Test
    void shouldCreateTransaction() throws Exception {
        // Given
        when(transactionService.create(any(TransactionDTO.class))).thenReturn(testTransactionDTO);

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTransactionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Test Transaction"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.type").value("INCOME"));

        verify(transactionService).create(any(TransactionDTO.class));
    }

    @Test
    void shouldFindTransactionById() throws Exception {
        // Given
        when(transactionService.findById(1L)).thenReturn(Optional.of(testTransactionDTO));

        // When & Then
        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Test Transaction"))
                .andExpect(jsonPath("$.amount").value(100.00));

        verify(transactionService).findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenTransactionDoesNotExist() throws Exception {
        // Given
        when(transactionService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/transactions/999"))
                .andExpect(status().isNotFound());

        verify(transactionService).findById(999L);
    }

    @Test
    void shouldFindAllTransactions() throws Exception {
        // Given
        List<TransactionDTO> transactions = List.of(testTransactionDTO);
        Page<TransactionDTO> page = new PageImpl<>(transactions, PageRequest.of(0, 10), 1);
        when(transactionService.findAll(anyString(), any(), anyString(), anyString(), any()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/transactions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].description").value("Test Transaction"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(transactionService).findAll(anyString(), any(), anyString(), anyString(), any());
    }

    @Test
    void shouldUpdateTransaction() throws Exception {
        // Given
        TransactionDTO updatedDTO = new TransactionDTO();
        updatedDTO.setId(1L);
        updatedDTO.setDescription("Updated Transaction");
        updatedDTO.setAmount(BigDecimal.valueOf(150.00));
        updatedDTO.setType(TransactionType.INCOME);
        updatedDTO.setSubtype(TransactionSubtype.FIXED);
        updatedDTO.setSource(TransactionSource.CASH);
        updatedDTO.setCategoryId(1L);
        updatedDTO.setUserId(1L);

        when(transactionService.update(eq(1L), any(TransactionDTO.class))).thenReturn(updatedDTO);

        // When & Then
        mockMvc.perform(put("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Updated Transaction"))
                .andExpect(jsonPath("$.amount").value(150.00));

        verify(transactionService).update(eq(1L), any(TransactionDTO.class));
    }

    @Test
    void shouldDeleteTransaction() throws Exception {
        // Given
        doNothing().when(transactionService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isNoContent());

        verify(transactionService).delete(1L);
    }

    @Test
    void shouldValidateRequiredFields() throws Exception {
        // Given - Create invalid transaction (missing required fields)
        TransactionDTO invalidDTO = new TransactionDTO();
        // Missing required fields: description, amount, type, etc.

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleSearchWithFilters() throws Exception {
        // Given
        List<TransactionDTO> transactions = List.of(testTransactionDTO);
        Page<TransactionDTO> page = new PageImpl<>(transactions, PageRequest.of(0, 10), 1);
        when(transactionService.findAll(anyString(), any(), anyString(), anyString(), any()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/transactions")
                        .param("search", "test")
                        .param("type", "INCOME")
                        .param("sortBy", "date")
                        .param("sortDirection", "desc")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].description").value("Test Transaction"));

        verify(transactionService).findAll(eq("test"), any(), eq("date"), eq("desc"), any());
    }

    @Test
    void shouldHandlePagination() throws Exception {
        // Given
        List<TransactionDTO> transactions = List.of(testTransactionDTO);
        Page<TransactionDTO> page = new PageImpl<>(transactions, PageRequest.of(1, 5), 1);
        when(transactionService.findAll(anyString(), any(), anyString(), anyString(), any()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/transactions")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5));

        verify(transactionService).findAll(anyString(), any(), anyString(), anyString(), any());
    }

    @Test
    void shouldHandleServiceException() throws Exception {
        // Given
        when(transactionService.create(any(TransactionDTO.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTransactionDTO)))
                .andExpect(status().isInternalServerError());
    }
}
