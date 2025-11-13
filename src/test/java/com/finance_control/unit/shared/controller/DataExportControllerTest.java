package com.finance_control.unit.shared.controller;

import com.finance_control.shared.service.DataExportService;
import com.finance_control.shared.security.CustomUserDetails;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DataExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataExportService dataExportService;

    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);
        testUserDetails = new CustomUserDetails(testUser);
    }

    @Test
    void exportAllDataAsCsv_ShouldReturnCsvFile() throws Exception {
        byte[] csvData = "id,name,amount\n1,Transaction 1,100.00".getBytes(StandardCharsets.UTF_8);
        when(dataExportService.exportUserDataAsCsv()).thenReturn(csvData);

        mockMvc.perform(get("/api/export/all/csv")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".csv")))
                .andExpect(header().longValue("Content-Length", csvData.length))
                .andExpect(content().bytes(csvData));

        verify(dataExportService, times(1)).exportUserDataAsCsv();
    }

    @Test
    void exportAllDataAsCsv_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(dataExportService.exportUserDataAsCsv())
                .thenThrow(new RuntimeException("Export failed"));

        mockMvc.perform(get("/api/export/all/csv")
                .with(user(testUserDetails)))
                .andExpect(status().isInternalServerError());

        verify(dataExportService, times(1)).exportUserDataAsCsv();
    }

    @Test
    void exportAllDataAsJson_ShouldReturnJsonData() throws Exception {
        String jsonData = "{\"user\":{\"id\":1,\"email\":\"test@example.com\"}}";
        when(dataExportService.exportUserDataAsJson()).thenReturn(jsonData);

        mockMvc.perform(get("/api/export/all/json")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(content().string(jsonData));

        verify(dataExportService, times(1)).exportUserDataAsJson();
    }

    @Test
    void exportAllDataAsJson_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(dataExportService.exportUserDataAsJson())
                .thenThrow(new RuntimeException("Export failed"));

        mockMvc.perform(get("/api/export/all/json")
                .with(user(testUserDetails)))
                .andExpect(status().isInternalServerError());

        verify(dataExportService, times(1)).exportUserDataAsJson();
    }

    @Test
    void exportTransactionsAsCsv_ShouldReturnCsvFile() throws Exception {
        byte[] csvData = "id,description,amount\n1,Transaction 1,100.00".getBytes(StandardCharsets.UTF_8);
        when(dataExportService.exportTransactionsAsCsv()).thenReturn(csvData);

        mockMvc.perform(get("/api/export/transactions/csv")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("transactions-export")))
                .andExpect(header().longValue("Content-Length", csvData.length))
                .andExpect(content().bytes(csvData));

        verify(dataExportService, times(1)).exportTransactionsAsCsv();
    }

    @Test
    void exportTransactionsAsCsv_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(dataExportService.exportTransactionsAsCsv())
                .thenThrow(new RuntimeException("Export failed"));

        mockMvc.perform(get("/api/export/transactions/csv")
                .with(user(testUserDetails)))
                .andExpect(status().isInternalServerError());

        verify(dataExportService, times(1)).exportTransactionsAsCsv();
    }

    @Test
    void exportFinancialGoalsAsCsv_ShouldReturnCsvFile() throws Exception {
        byte[] csvData = "id,name,targetAmount\n1,Goal 1,10000.00".getBytes(StandardCharsets.UTF_8);
        when(dataExportService.exportFinancialGoalsAsCsv()).thenReturn(csvData);

        mockMvc.perform(get("/api/export/goals/csv")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("financial-goals-export")))
                .andExpect(header().longValue("Content-Length", csvData.length))
                .andExpect(content().bytes(csvData));

        verify(dataExportService, times(1)).exportFinancialGoalsAsCsv();
    }

    @Test
    void exportFinancialGoalsAsCsv_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(dataExportService.exportFinancialGoalsAsCsv())
                .thenThrow(new RuntimeException("Export failed"));

        mockMvc.perform(get("/api/export/goals/csv")
                .with(user(testUserDetails)))
                .andExpect(status().isInternalServerError());

        verify(dataExportService, times(1)).exportFinancialGoalsAsCsv();
    }
}
