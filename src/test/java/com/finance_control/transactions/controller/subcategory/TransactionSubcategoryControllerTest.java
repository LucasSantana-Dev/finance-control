package com.finance_control.transactions.controller.subcategory;

import com.finance_control.transactions.dto.subcategory.TransactionSubcategoryDTO;
import com.finance_control.transactions.service.subcategory.TransactionSubcategoryService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionSubcategoryController.class)
class TransactionSubcategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionSubcategoryService transactionSubcategoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransactionSubcategoryDTO sampleSubcategory;
    private Page<TransactionSubcategoryDTO> samplePage;

    @BeforeEach
    void setUp() {
        sampleSubcategory = new TransactionSubcategoryDTO();
        sampleSubcategory.setId(1L);
        sampleSubcategory.setName("Restaurants");
        sampleSubcategory.setDescription("Restaurant expenses");
        sampleSubcategory.setCategoryId(1L);
        sampleSubcategory.setIsActive(true);
        sampleSubcategory.setCreatedAt(LocalDateTime.now());

        List<TransactionSubcategoryDTO> subcategories = Arrays.asList(sampleSubcategory);
        samplePage = new PageImpl<>(subcategories, PageRequest.of(0, 20), 1);
    }

    @Test
    void getTransactionSubcategories_WithValidParameters_ShouldReturnOk() throws Exception {
        when(transactionSubcategoryService.findAll(anyString(), any(Map.class), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transaction-subcategories")
                .param("categoryId", "1")
                .param("search", "restaurant")
                .param("sortBy", "name")
                .param("sortDirection", "asc")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Restaurants"))
                .andExpect(jsonPath("$.content[0].description").value("Restaurant expenses"))
                .andExpect(jsonPath("$.content[0].categoryId").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getTransactionSubcategories_WithCategoryIdAndSortByUsage_ShouldReturnOk() throws Exception {
        when(transactionSubcategoryService.findByCategoryIdOrderByUsage(anyLong(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transaction-subcategories")
                .param("categoryId", "1")
                .param("sortByUsage", "true")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactionSubcategories_WithDefaultParameters_ShouldReturnOk() throws Exception {
        when(transactionSubcategoryService.findAll(anyString(), any(Map.class), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/transaction-subcategories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactionSubcategories_WithAllData_ShouldReturnOk() throws Exception {
        List<TransactionSubcategoryDTO> allSubcategories = Arrays.asList(sampleSubcategory);
        when(transactionSubcategoryService.findAllActive())
                .thenReturn(allSubcategories);

        mockMvc.perform(get("/transaction-subcategories")
                .param("data", "all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Restaurants"));
    }

    @Test
    void getTransactionSubcategories_WithByCategoryData_ShouldReturnOk() throws Exception {
        List<TransactionSubcategoryDTO> subcategories = Arrays.asList(sampleSubcategory);
        when(transactionSubcategoryService.findByCategoryId(anyLong()))
                .thenReturn(subcategories);

        mockMvc.perform(get("/transaction-subcategories")
                .param("data", "by-category")
                .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Restaurants"));
    }

    @Test
    void getTransactionSubcategories_WithByCategoryDataWithoutCategoryId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/transaction-subcategories")
                .param("data", "by-category"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactionSubcategories_WithByCategoryUsageData_ShouldReturnOk() throws Exception {
        List<TransactionSubcategoryDTO> subcategories = Arrays.asList(sampleSubcategory);
        when(transactionSubcategoryService.findByCategoryIdOrderByUsage(anyLong()))
                .thenReturn(subcategories);

        mockMvc.perform(get("/transaction-subcategories")
                .param("data", "by-category-usage")
                .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Restaurants"));
    }

    @Test
    void getTransactionSubcategories_WithCountData_ShouldReturnOk() throws Exception {
        when(transactionSubcategoryService.getTotalCount())
                .thenReturn(15L);

        mockMvc.perform(get("/transaction-subcategories")
                .param("data", "count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(15));
    }

    @Test
    void getTransactionSubcategories_WithCountByCategoryData_ShouldReturnOk() throws Exception {
        when(transactionSubcategoryService.countByCategoryId(anyLong()))
                .thenReturn(3L);

        mockMvc.perform(get("/transaction-subcategories")
                .param("data", "count-by-category")
                .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(3));
    }

    @Test
    void getTransactionSubcategories_WithCountByCategoryDataWithoutCategoryId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/transaction-subcategories")
                .param("data", "count-by-category"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactionSubcategories_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/transaction-subcategories")
                .param("data", "invalid-type"))
                .andExpect(status().isBadRequest());
    }
}
