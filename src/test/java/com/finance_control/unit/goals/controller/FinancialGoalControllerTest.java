package com.finance_control.unit.goals.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.goals.controller.FinancialGoalController;
import com.finance_control.goals.dto.FinancialGoalDTO;
import com.finance_control.goals.service.FinancialGoalService;
import com.finance_control.shared.enums.GoalType;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @WebMvcTest controller layer test for FinancialGoalController.
 * Tests REST API endpoints with mocked service layer.
 *
 * This is the industry standard approach for testing controllers in Spring Boot.
 */
@WebMvcTest(FinancialGoalController.class)
class FinancialGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FinancialGoalService financialGoalService;

    private FinancialGoalDTO testGoalDTO;

    @BeforeEach
    void setUp() {
        testGoalDTO = new FinancialGoalDTO();
        testGoalDTO.setId(1L);
        testGoalDTO.setName("Test Goal");
        testGoalDTO.setDescription("Test Description");
        testGoalDTO.setGoalType(GoalType.SAVINGS);
        testGoalDTO.setTargetAmount(BigDecimal.valueOf(10000.00));
        testGoalDTO.setCurrentAmount(BigDecimal.valueOf(1000.00));
        testGoalDTO.setTargetDate(LocalDateTime.now().plusMonths(12));
        testGoalDTO.setDeadline(LocalDateTime.now().plusMonths(12));
        testGoalDTO.setIsActive(true);
        testGoalDTO.setAutoCalculate(false);
    }

    @Test
    void shouldCreateFinancialGoal() throws Exception {
        // Given
        when(financialGoalService.create(any(FinancialGoalDTO.class))).thenReturn(testGoalDTO);

        // When & Then
        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testGoalDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Goal"))
                .andExpect(jsonPath("$.targetAmount").value(10000.00))
                .andExpect(jsonPath("$.goalType").value("SAVINGS"));

        verify(financialGoalService).create(any(FinancialGoalDTO.class));
    }

    @Test
    void shouldFindGoalById() throws Exception {
        // Given
        when(financialGoalService.findById(1L)).thenReturn(Optional.of(testGoalDTO));

        // When & Then
        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Goal"))
                .andExpect(jsonPath("$.targetAmount").value(10000.00));

        verify(financialGoalService).findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenGoalDoesNotExist() throws Exception {
        // Given
        when(financialGoalService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/goals/999"))
                .andExpect(status().isNotFound());

        verify(financialGoalService).findById(999L);
    }

    @Test
    void shouldFindAllGoals() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> page = new PageImpl<>(goals, PageRequest.of(0, 10), 1);
        when(financialGoalService.findAll(anyString(), any(), anyString(), anyString(), any()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/goals")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Test Goal"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(financialGoalService).findAll(anyString(), any(), anyString(), anyString(), any());
    }

    @Test
    void shouldUpdateGoal() throws Exception {
        // Given
        FinancialGoalDTO updatedDTO = new FinancialGoalDTO();
        updatedDTO.setId(1L);
        updatedDTO.setName("Updated Goal");
        updatedDTO.setDescription("Updated Description");
        updatedDTO.setGoalType(GoalType.INVESTMENT);
        updatedDTO.setTargetAmount(BigDecimal.valueOf(15000.00));
        updatedDTO.setCurrentAmount(BigDecimal.valueOf(1500.00));
        updatedDTO.setTargetDate(LocalDateTime.now().plusMonths(18));
        updatedDTO.setDeadline(LocalDateTime.now().plusMonths(18));
        updatedDTO.setIsActive(true);
        updatedDTO.setAutoCalculate(false);

        when(financialGoalService.update(eq(1L), any(FinancialGoalDTO.class))).thenReturn(updatedDTO);

        // When & Then
        mockMvc.perform(put("/api/goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Goal"))
                .andExpect(jsonPath("$.targetAmount").value(15000.00));

        verify(financialGoalService).update(eq(1L), any(FinancialGoalDTO.class));
    }

    @Test
    void shouldDeleteGoal() throws Exception {
        // Given
        doNothing().when(financialGoalService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/goals/1"))
                .andExpect(status().isNoContent());

        verify(financialGoalService).delete(1L);
    }

    @Test
    void shouldValidateRequiredFields() throws Exception {
        // Given - Create invalid goal (missing required fields)
        FinancialGoalDTO invalidDTO = new FinancialGoalDTO();
        // Missing required fields: name, goalType, targetAmount, etc.

        // When & Then
        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleSearchWithFilters() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> page = new PageImpl<>(goals, PageRequest.of(0, 10), 1);
        when(financialGoalService.findAll(anyString(), any(), anyString(), anyString(), any()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/goals")
                        .param("search", "test")
                        .param("goalType", "SAVINGS")
                        .param("sortBy", "targetDate")
                        .param("sortDirection", "asc")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Goal"));

        verify(financialGoalService).findAll(eq("test"), any(), eq("targetDate"), eq("asc"), any());
    }

    @Test
    void shouldHandlePagination() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> page = new PageImpl<>(goals, PageRequest.of(1, 5), 1);
        when(financialGoalService.findAll(anyString(), any(), anyString(), anyString(), any()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/goals")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5));

        verify(financialGoalService).findAll(anyString(), any(), anyString(), anyString(), any());
    }

    @Test
    void shouldHandleServiceException() throws Exception {
        // Given
        when(financialGoalService.create(any(FinancialGoalDTO.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testGoalDTO)))
                .andExpect(status().isInternalServerError());
    }
}
