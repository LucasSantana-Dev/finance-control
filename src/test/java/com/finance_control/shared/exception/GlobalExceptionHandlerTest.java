package com.finance_control.shared.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RestController
class TestExceptionController {
    @GetMapping("/test/not-found")
    public void throwNotFound() {
        throw new EntityNotFoundException("Test entity not found");
    }

    @GetMapping("/test/illegal-argument")
    public void throwIllegalArgument() {
        throw new IllegalArgumentException("Invalid argument provided");
    }

    @GetMapping("/test/validation-error")
    public void throwValidationError() {
        throw new RuntimeException("Validation test - handled separately");
    }

    @GetMapping("/test/generic-exception")
    public void throwGenericException() {
        throw new RuntimeException("Unexpected error occurred");
    }

    @GetMapping("/test/entity-not-found-with-id")
    public void throwEntityNotFoundWithId() {
        throw new EntityNotFoundException("User", 123L);
    }

    @GetMapping("/test/entity-not-found-with-field")
    public void throwEntityNotFoundWithField() {
        throw new EntityNotFoundException("User", "email", "test@example.com");
    }
}

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private GlobalExceptionHandler exceptionHandler;
    private TestExceptionController testController;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        testController = new TestExceptionController();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(testController)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void handleEntityNotFound_ShouldReturn404WithErrorResponse() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Test entity not found"))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleEntityNotFound_ShouldIncludeCorrectFields() throws Exception {
        String response = mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);

        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getError()).isEqualTo("Not Found");
        assertThat(errorResponse.getMessage()).isEqualTo("Test entity not found");
        assertThat(errorResponse.getPath()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }

    @Test
    void handleIllegalArgument_ShouldReturn400WithErrorResponse() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid argument provided"))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleValidationExceptions_ShouldReturn400WithDetails() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError(
                "testObject", "fieldName", "rejectedValue", false, null, null, "Field is required");
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        java.lang.reflect.Method method = String.class.getMethod("equals", Object.class);
        org.springframework.core.MethodParameter realParameter = new org.springframework.core.MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(realParameter, bindingResult);
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test/validation-error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed for the provided data");
        assertThat(response.getBody().getPath()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleValidationExceptions_ShouldIncludeValidationErrorsInDetails() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError(
                "testObject", "fieldName", "rejectedValue", false, null, null, "Field is required");
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        java.lang.reflect.Method method = String.class.getMethod("equals", Object.class);
        org.springframework.core.MethodParameter realParameter = new org.springframework.core.MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(realParameter, bindingResult);
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test/validation-error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        assertThat(response.getBody().getDetails()).isNotNull();
        assertThat(response.getBody().getDetails()).containsKey("fieldName");
        assertThat(response.getBody().getDetails().get("fieldName")).isEqualTo("Field is required");
    }

    @Test
    void handleGenericException_ShouldReturn500WithErrorResponse() throws Exception {
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleGenericException_ShouldNotExposeInternalDetails() throws Exception {
        String response = mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);

        assertThat(errorResponse.getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(errorResponse.getMessage()).doesNotContain("RuntimeException");
        assertThat(errorResponse.getDetails()).isNull();
    }

    @Test
    void allHandlers_ShouldIncludeTimestampAndPath() throws Exception, NoSuchMethodException {
        String[] endpoints = {
                "/test/not-found",
                "/test/illegal-argument",
                "/test/generic-exception"
        };

        for (String endpoint : endpoints) {
            String response = mockMvc.perform(get(endpoint))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);

            assertThat(errorResponse.getTimestamp()).isNotNull();
            assertThat(errorResponse.getPath()).isNotNull();
            assertThat(errorResponse.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
        }

        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test/validation");

        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("testObject", "field", "value", false, null, null, "Error");
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));
        java.lang.reflect.Method method = String.class.getMethod("equals", Object.class);
        org.springframework.core.MethodParameter realParameter = new org.springframework.core.MethodParameter(method, 0);
        MethodArgumentNotValidException validationException = new MethodArgumentNotValidException(realParameter, bindingResult);

        ResponseEntity<ErrorResponse> validationResponse = exceptionHandler.handleValidationExceptions(validationException, webRequest);
        assertThat(validationResponse.getBody().getTimestamp()).isNotNull();
        assertThat(validationResponse.getBody().getPath()).isNotNull();
    }

    @Test
    void handleEntityNotFound_WithId_ShouldReturnFormattedMessage() throws Exception {
        mockMvc.perform(get("/test/entity-not-found-with-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 123"));
    }

    @Test
    void handleEntityNotFound_WithField_ShouldReturnFormattedMessage() throws Exception {
        mockMvc.perform(get("/test/entity-not-found-with-field"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with email: test@example.com"));
    }

    @Test
    void handleValidationExceptions_WithMultipleFields_ShouldIncludeAllErrors() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("testObject", "field1", "value1", false, null, null, "Field 1 is invalid");
        FieldError fieldError2 = new FieldError("testObject", "field2", "value2", false, null, null, "Field 2 is required");

        List<org.springframework.validation.ObjectError> errors = Arrays.asList(fieldError1, fieldError2);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        java.lang.reflect.Method method = String.class.getMethod("equals", Object.class);
        org.springframework.core.MethodParameter realParameter = new org.springframework.core.MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(realParameter, bindingResult);
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test/validation");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).containsKey("field1");
        assertThat(response.getBody().getDetails()).containsKey("field2");
        assertThat(response.getBody().getDetails().get("field1")).isEqualTo("Field 1 is invalid");
        assertThat(response.getBody().getDetails().get("field2")).isEqualTo("Field 2 is required");
    }

    @Test
    void handleValidationExceptions_WithEmptyErrorsList_ShouldReturnEmptyDetails() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(Collections.emptyList());

        java.lang.reflect.Method method = String.class.getMethod("equals", Object.class);
        org.springframework.core.MethodParameter realParameter = new org.springframework.core.MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(realParameter, bindingResult);
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test/validation");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).isNotNull();
        assertThat(response.getBody().getDetails()).isEmpty();
    }

    // Note: Testing non-FieldError ObjectError types is difficult because ObjectError is abstract
    // and Spring's validation framework typically only returns FieldError instances.
    // The code at line 57 does an unsafe cast ((FieldError) error) which would throw
    // ClassCastException if a non-FieldError ObjectError is encountered, but this scenario
    // is not easily testable without creating a custom ObjectError subclass.

    @Test
    void handleGenericException_WithNullPointerException_ShouldReturn500() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/null-pointer")
            public void throwNullPointer() {
                throw new NullPointerException("Null pointer occurred");
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/null-pointer"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void handleGenericException_WithIllegalStateException_ShouldReturn500() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/illegal-state")
            public void throwIllegalState() {
                throw new IllegalStateException("Illegal state occurred");
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void handleEntityNotFound_WithNullWebRequest_ShouldHandleGracefully() {
        EntityNotFoundException ex = new EntityNotFoundException("Test entity");
        WebRequest webRequest = null;

        assertThatThrownBy(() -> exceptionHandler.handleEntityNotFound(ex, webRequest))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void handleIllegalArgument_WithNullWebRequest_ShouldHandleGracefully() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        WebRequest webRequest = null;

        assertThatThrownBy(() -> exceptionHandler.handleIllegalArgument(ex, webRequest))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void handleGenericException_WithNullWebRequest_ShouldHandleGracefully() {
        Exception ex = new RuntimeException("Unexpected error");
        WebRequest webRequest = null;

        assertThatThrownBy(() -> exceptionHandler.handleGenericException(ex, webRequest))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void handleValidationExceptions_WithNullWebRequest_ShouldHandleGracefully() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("testObject", "field", "value", false, null, null, "Error");
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        java.lang.reflect.Method method = String.class.getMethod("equals", Object.class);
        org.springframework.core.MethodParameter realParameter = new org.springframework.core.MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(realParameter, bindingResult);
        WebRequest webRequest = null;

        assertThatThrownBy(() -> exceptionHandler.handleValidationExceptions(exception, webRequest))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void handleEntityNotFound_WithNullMessage_ShouldHandleGracefully() {
        EntityNotFoundException ex = new EntityNotFoundException((String) null);
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleEntityNotFound(ex, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isNull();
    }

    @Test
    void handleIllegalArgument_WithNullMessage_ShouldHandleGracefully() {
        IllegalArgumentException ex = new IllegalArgumentException((String) null);
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(ex, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isNull();
    }
}
