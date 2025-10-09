package com.finance_control.unit.transactions.controller.subcategory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.transactions.controller.subcategory.TransactionSubcategoryController;
import com.finance_control.transactions.dto.subcategory.TransactionSubcategoryDTO;
import com.finance_control.transactions.service.subcategory.TransactionSubcategoryService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionSubcategoryControllerTest {

    @Mock
    private TransactionSubcategoryService transactionSubcategoryService;

    @InjectMocks
    private TransactionSubcategoryController transactionSubcategoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TransactionSubcategoryDTO testSubcategoryDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionSubcategoryController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testSubcategoryDTO = new TransactionSubcategoryDTO();
        testSubcategoryDTO.setId(1L);
        testSubcategoryDTO.setName("Test Subcategory");
        testSubcategoryDTO.setDescription("Test Description");
        testSubcategoryDTO.setCategoryId(1L);
        testSubcategoryDTO.setIsActive(true);
        testSubcategoryDTO.setCreatedAt(LocalDateTime.now());
        testSubcategoryDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createTransactionSubcategory_ShouldReturnCreatedSubcategory() throws Exception {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName("New Subcategory");
        createDTO.setDescription("New Description");
        createDTO.setCategoryId(1L);

        when(transactionSubcategoryService.create(any(TransactionSubcategoryDTO.class)))
                .thenReturn(testSubcategoryDTO);

        // When & Then
        mockMvc.perform(post("/transaction-subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Subcategory"))
                .andExpect(jsonPath("$.categoryId").value(1L));

        verify(transactionSubcategoryService).create(any(TransactionSubcategoryDTO.class));
    }

    @Test
    void getTransactionSubcategoryById_ShouldReturnSubcategory() throws Exception {
        // Given
        when(transactionSubcategoryService.findById(1L)).thenReturn(Optional.of(testSubcategoryDTO));

        // When & Then
        mockMvc.perform(get("/transaction-subcategories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Subcategory"))
                .andExpect(jsonPath("$.categoryId").value(1L));

        verify(transactionSubcategoryService).findById(1L);
    }

    @Test
    void getAllTransactionSubcategories_ShouldReturnPaginatedSubcategories() throws Exception {
        // Given
        List<TransactionSubcategoryDTO> subcategories = List.of(testSubcategoryDTO);
        Page<TransactionSubcategoryDTO> page = new PageImpl<>(subcategories, PageRequest.of(0, 10), 1);

        when(transactionSubcategoryService.findAll(anyString(), anyMap(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/transaction-subcategories")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Test Subcategory"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(transactionSubcategoryService).findAll(anyString(), anyMap(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void updateTransactionSubcategory_ShouldReturnUpdatedSubcategory() throws Exception {
        // Given
        TransactionSubcategoryDTO updateDTO = new TransactionSubcategoryDTO();
        updateDTO.setName("Updated Subcategory");
        updateDTO.setDescription("Updated Description");

        when(transactionSubcategoryService.update(eq(1L), any(TransactionSubcategoryDTO.class)))
                .thenReturn(testSubcategoryDTO);

        // When & Then
        mockMvc.perform(put("/transaction-subcategories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Subcategory"));

        verify(transactionSubcategoryService).update(eq(1L), any(TransactionSubcategoryDTO.class));
    }

    @Test
    void deleteTransactionSubcategory_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(transactionSubcategoryService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/transaction-subcategories/1"))
                .andExpect(status().isNoContent());

        verify(transactionSubcategoryService).delete(1L);
    }

    @Test
    void getTransactionSubcategoriesByCategory_ShouldReturnSubcategories() throws Exception {
        // Given
        List<TransactionSubcategoryDTO> subcategories = List.of(testSubcategoryDTO);

        when(transactionSubcategoryService.findByCategoryId(1L)).thenReturn(subcategories);

        // When & Then
        mockMvc.perform(get("/transaction-subcategories/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Subcategory"))
                .andExpect(jsonPath("$[0].categoryId").value(1L));

        verify(transactionSubcategoryService).findByCategoryId(1L);
    }

    @Test
    void getTransactionSubcategoriesByCategoryOrderByUsage_ShouldReturnOrderedSubcategories() throws Exception {
        // Given
        List<TransactionSubcategoryDTO> subcategories = List.of(testSubcategoryDTO);

        when(transactionSubcategoryService.findByCategoryIdOrderByUsage(1L)).thenReturn(subcategories);

        // When & Then
        mockMvc.perform(get("/transaction-subcategories/category/1/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Subcategory"));

        verify(transactionSubcategoryService).findByCategoryIdOrderByUsage(1L);
    }

    @Test
    void getTransactionSubcategoryCountByCategory_ShouldReturnCount() throws Exception {
        // Given
        when(transactionSubcategoryService.countByCategoryId(1L)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/transaction-subcategories/category/1/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(transactionSubcategoryService).countByCategoryId(1L);
    }

    @Test
    void createTransactionSubcategory_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionSubcategoryDTO invalidDTO = new TransactionSubcategoryDTO();
        // name and categoryId are null, which should cause validation error

        // When & Then
        mockMvc.perform(post("/transaction-subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(transactionSubcategoryService, never()).create(any(TransactionSubcategoryDTO.class));
    }

    @Test
    void getTransactionSubcategories_WithSearch_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<TransactionSubcategoryDTO> subcategories = List.of(testSubcategoryDTO);
        Page<TransactionSubcategoryDTO> page = new PageImpl<>(subcategories, PageRequest.of(0, 10), 1);

        when(transactionSubcategoryService.findAll(eq("test"), anyMap(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/transaction-subcategories")
                        .param("search", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Subcategory"));

        verify(transactionSubcategoryService).findAll(eq("test"), anyMap(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void getTransactionSubcategories_WithCategoryFilter_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<TransactionSubcategoryDTO> subcategories = List.of(testSubcategoryDTO);
        Page<TransactionSubcategoryDTO> page = new PageImpl<>(subcategories, PageRequest.of(0, 10), 1);

        when(transactionSubcategoryService.findAll(anyString(), anyMap(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/transaction-subcategories")
                        .param("categoryId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].categoryId").value(1L));

        verify(transactionSubcategoryService).findAll(anyString(), anyMap(), anyString(), anyString(), any(Pageable.class));
    }
}
