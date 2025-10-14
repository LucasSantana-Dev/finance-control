package com.finance_control.transactions.controller.category;

import com.finance_control.shared.exception.GlobalExceptionHandler;
import com.finance_control.transactions.dto.category.TransactionCategoryDTO;
import com.finance_control.transactions.service.category.TransactionCategoryService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionCategoryControllerTest {

    @Mock
    private TransactionCategoryService transactionCategoryService;

    @InjectMocks
    private TransactionCategoryController transactionCategoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TransactionCategoryDTO sampleCategory;
    private Page<TransactionCategoryDTO> samplePage;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionCategoryController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        sampleCategory = new TransactionCategoryDTO();
        sampleCategory.setId(1L);
        sampleCategory.setName("Food & Dining");

        List<TransactionCategoryDTO> categories = Arrays.asList(sampleCategory);
        samplePage = new PageImpl<>(categories, PageRequest.of(0, 20), 1);
    }

    @Test
    void getTransactionCategories_WithValidParameters_ShouldReturnOk() throws Exception {
        when(transactionCategoryService.findAll(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transaction-categories")
                .param("search", "food")
                .param("sortBy", "name")
                .param("sortDirection", "asc")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Food & Dining"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getTransactionCategories_WithDefaultParameters_ShouldReturnOk() throws Exception {
        when(transactionCategoryService.findAll(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transaction-categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactionCategories_WithAllData_ShouldReturnOk() throws Exception {
        List<TransactionCategoryDTO> allCategories = Arrays.asList(sampleCategory);
        when(transactionCategoryService.findAllActive())
                .thenReturn(allCategories);

        mockMvc.perform(get("/transaction-categories/metadata")
                .param("data", "all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Food & Dining"));
    }

    @Test
    void getTransactionCategories_WithCountData_ShouldReturnOk() throws Exception {
        when(transactionCategoryService.getTotalCount())
                .thenReturn(5L);

        mockMvc.perform(get("/transaction-categories/metadata")
                .param("data", "count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(5));
    }

    @Test
    void getTransactionCategories_WithUsageStatsData_ShouldReturnOk() throws Exception {
        Map<String, Object> usageStats = Map.of(
                "totalCategories", 10,
                "usedCategories", 7
        );
        when(transactionCategoryService.getUsageStats())
                .thenReturn(usageStats);

        mockMvc.perform(get("/transaction-categories/metadata")
                .param("data", "usage-stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCategories").value(10))
                .andExpect(jsonPath("$.usedCategories").value(7));
    }

    @Test
    void getTransactionCategories_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/transaction-categories/metadata")
                .param("data", "invalid-type"))
                .andExpect(status().isBadRequest());
    }
}
