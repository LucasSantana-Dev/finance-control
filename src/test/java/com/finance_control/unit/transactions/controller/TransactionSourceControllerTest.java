package com.finance_control.unit.transactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.transactions.dto.source.TransactionSourceDTO;
import com.finance_control.transactions.service.source.TransactionSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.finance_control.shared.exception.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.finance_control.transactions.controller.source.TransactionSourceController;
import com.finance_control.unit.BaseWebMvcTest;

@WebMvcTest(controllers = TransactionSourceController.class)
@Import({ GlobalExceptionHandler.class })
class TransactionSourceControllerTest extends BaseWebMvcTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private TransactionSourceService transactionSourceService;

        @Autowired
        private ObjectMapper objectMapper;

        private TransactionSourceDTO createDTO;
        private TransactionSourceDTO responseDTO;

        @BeforeEach
        void setUp() {
                transactionSourceService = mock(TransactionSourceService.class);
                createDTO = new TransactionSourceDTO();
                createDTO.setName("Nubank Credit Card");
                createDTO.setDescription("Main credit card");
                createDTO.setSourceType(TransactionSource.CREDIT_CARD);
                createDTO.setBankName("Nubank");
                createDTO.setCardType("Credit");
                createDTO.setCardLastFour("1234");
                createDTO.setAccountBalance(new BigDecimal("5000.00"));
                createDTO.setUserId(1L);

                responseDTO = new TransactionSourceDTO();
                responseDTO.setId(1L);
                responseDTO.setName("Nubank Credit Card");
                responseDTO.setDescription("Main credit card");
                responseDTO.setSourceType(TransactionSource.CREDIT_CARD);
                responseDTO.setBankName("Nubank");
                responseDTO.setCardType("Credit");
                responseDTO.setCardLastFour("1234");
                responseDTO.setAccountBalance(new BigDecimal("5000.00"));
                responseDTO.setIsActive(true);
                responseDTO.setUserId(1L);
                responseDTO.setCreatedAt(LocalDateTime.now());
                responseDTO.setUpdatedAt(LocalDateTime.now());
        }

        @Test
        void createTransactionSource_ShouldReturnCreated() throws Exception {
                when(transactionSourceService.create(any(TransactionSourceDTO.class)))
                                .thenReturn(responseDTO);

                mockMvc.perform(post("/transaction-sources")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Nubank Credit Card"))
                                .andExpect(jsonPath("$.sourceType").value("CREDIT_CARD"))
                                .andExpect(jsonPath("$.bankName").value("Nubank"))
                                .andExpect(jsonPath("$.userId").value(1));
        }

        @Test
        void createTransactionSource_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
                createDTO.setName(""); // Invalid name

                mockMvc.perform(post("/transaction-sources")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDTO)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void updateTransactionSource_ShouldReturnOk() throws Exception {
                when(transactionSourceService.update(anyLong(), any(TransactionSourceDTO.class)))
                                .thenReturn(responseDTO);

                mockMvc.perform(put("/transaction-sources/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Nubank Credit Card"));
        }

        @Test
        void getTransactionSource_ShouldReturnOk_WhenExists() throws Exception {
                when(transactionSourceService.findById(1L, 1L))
                                .thenReturn(Optional.of(responseDTO));

                mockMvc.perform(get("/transaction-sources/1")
                                .param("userId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Nubank Credit Card"));
        }

        @Test
        void getTransactionSource_ShouldReturnNotFound_WhenNotExists() throws Exception {
                when(transactionSourceService.findById(1L, 1L))
                                .thenReturn(Optional.empty());

                mockMvc.perform(get("/transaction-sources/1")
                                .param("userId", "1"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void getTransactionSources_ShouldReturnAllSources() throws Exception {
                TransactionSourceDTO source2 = new TransactionSourceDTO();
                source2.setId(2L);
                source2.setName("Itaú Account");
                source2.setUserId(1L);

                List<TransactionSourceDTO> sources = Arrays.asList(responseDTO, source2);
                when(transactionSourceService.findByUserId(1L))
                                .thenReturn(sources);

                mockMvc.perform(get("/transaction-sources")
                                .param("userId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].name").value("Nubank Credit Card"))
                                .andExpect(jsonPath("$[1].id").value(2))
                                .andExpect(jsonPath("$[1].name").value("Itaú Account"));
        }

        @Test
        void getTransactionSources_ShouldReturnActiveSourcesOnly() throws Exception {
                when(transactionSourceService.findActiveByUserId(1L))
                                .thenReturn(Arrays.asList(responseDTO));

                mockMvc.perform(get("/transaction-sources")
                                .param("userId", "1")
                                .param("activeOnly", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].isActive").value(true));
        }

        @Test
        void deleteTransactionSource_ShouldReturnNoContent() throws Exception {
                mockMvc.perform(delete("/transaction-sources/1")
                                .param("userId", "1"))
                                .andExpect(status().isNoContent());
        }
}