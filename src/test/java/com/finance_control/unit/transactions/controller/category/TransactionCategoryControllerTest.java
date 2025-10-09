package com.finance_control.unit.transactions.controller.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.transactions.controller.category.TransactionCategoryController;
import com.finance_control.transactions.dto.category.TransactionCategoryDTO;
import com.finance_control.transactions.service.category.TransactionCategoryService;
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
class TransactionCategoryControllerTest {

    @Mock
    private TransactionCategoryService transactionCategoryService;

    @InjectMocks
    private TransactionCategoryController transactionCategoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TransactionCategoryDTO testCategoryDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionCategoryController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testCategoryDTO = new TransactionCategoryDTO();
        testCategoryDTO.setId(1L);
        testCategoryDTO.setName("Test Category");
        testCategoryDTO.setCreatedAt(LocalDateTime.now());
        testCategoryDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createTransactionCategory_ShouldReturnCreatedCategory() throws Exception {
        // Given
        TransactionCategoryDTO createDTO = new TransactionCategoryDTO();
        createDTO.setName("New Category");

        when(transactionCategoryService.create(any(TransactionCategoryDTO.class)))
                .thenReturn(testCategoryDTO);

        // When & Then
        mockMvc.perform(post("/transaction-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Category"));

        verify(transactionCategoryService).create(any(TransactionCategoryDTO.class));
    }

    @Test
    void getTransactionCategoryById_ShouldReturnCategory() throws Exception {
        // Given
        when(transactionCategoryService.findById(1L)).thenReturn(Optional.of(testCategoryDTO));

        // When & Then
        mockMvc.perform(get("/transaction-categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Category"));

        verify(transactionCategoryService).findById(1L);
    }

    @Test
    void getAllTransactionCategories_ShouldReturnPaginatedCategories() throws Exception {
        // Given
        List<TransactionCategoryDTO> categories = List.of(testCategoryDTO);
        Page<TransactionCategoryDTO> page = new PageImpl<>(categories, PageRequest.of(0, 10), 1);

        when(transactionCategoryService.findAll(anyString(), anyMap(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/transaction-categories")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Test Category"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(transactionCategoryService).findAll(anyString(), anyMap(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void updateTransactionCategory_ShouldReturnUpdatedCategory() throws Exception {
        // Given
        TransactionCategoryDTO updateDTO = new TransactionCategoryDTO();
        updateDTO.setName("Updated Category");

        when(transactionCategoryService.update(eq(1L), any(TransactionCategoryDTO.class)))
                .thenReturn(testCategoryDTO);

        // When & Then
        mockMvc.perform(put("/transaction-categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Category"));

        verify(transactionCategoryService).update(eq(1L), any(TransactionCategoryDTO.class));
    }

    @Test
    void deleteTransactionCategory_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(transactionCategoryService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/transaction-categories/1"))
                .andExpect(status().isNoContent());

        verify(transactionCategoryService).delete(1L);
    }

    @Test
    void createTransactionCategory_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionCategoryDTO invalidDTO = new TransactionCategoryDTO();
        // name is null, which should cause validation error

        // When & Then
        mockMvc.perform(post("/transaction-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(transactionCategoryService, never()).create(any(TransactionCategoryDTO.class));
    }

    @Test
    void getTransactionCategories_WithSearch_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<TransactionCategoryDTO> categories = List.of(testCategoryDTO);
        Page<TransactionCategoryDTO> page = new PageImpl<>(categories, PageRequest.of(0, 10), 1);

        when(transactionCategoryService.findAll(eq("test"), anyMap(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/transaction-categories")
                        .param("search", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Category"));

        verify(transactionCategoryService).findAll(eq("test"), anyMap(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void getTransactionCategories_WithSorting_ShouldReturnSortedResults() throws Exception {
        // Given
        List<TransactionCategoryDTO> categories = List.of(testCategoryDTO);
        Page<TransactionCategoryDTO> page = new PageImpl<>(categories, PageRequest.of(0, 10), 1);

        when(transactionCategoryService.findAll(anyString(), anyMap(), eq("name"), eq("desc"), any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/transaction-categories")
                        .param("sortBy", "name")
                        .param("sortDir", "desc")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(transactionCategoryService).findAll(anyString(), anyMap(), eq("name"), eq("desc"), any(Pageable.class));
    }
}
