package com.finance_control.unit.shared.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finance_control.shared.controller.FrontendErrorController;
import com.finance_control.shared.dto.FrontendErrorReportRequest;
import com.finance_control.shared.dto.FrontendErrorReportResponse;
import com.finance_control.shared.error.FrontendErrorService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class FrontendErrorControllerTest {

    @Mock
    private FrontendErrorService frontendErrorService;

    @InjectMocks
    private FrontendErrorController frontendErrorController;

    private FrontendErrorReportRequest request;

    @BeforeEach
    void setUp() {
        request = FrontendErrorReportRequest.builder()
            .message("ReferenceError: foo is not defined")
            .severity("HIGH")
            .occurredAt(Instant.now())
            .build();
    }

    @Test
    void submitError_ShouldReturnAcceptedResponse() {
        FrontendErrorReportResponse response = FrontendErrorReportResponse.builder()
            .id(10L)
            .status("RECEIVED")
            .receivedAt(Instant.now())
            .build();
        when(frontendErrorService.logFrontendError(any(FrontendErrorReportRequest.class))).thenReturn(response);

        ResponseEntity<FrontendErrorReportResponse> result = frontendErrorController.submitError(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(result.getBody()).isEqualTo(response);
        verify(frontendErrorService).logFrontendError(any(FrontendErrorReportRequest.class));
    }
}
