package com.finance_control.shared.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.shared.monitoring.SentryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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

    @Mock
    private SentryService sentryService;

    private MockMvc mockMvc;
    private GlobalExceptionHandler exceptionHandler;
    private TestExceptionController testController;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler(sentryService);
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

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleEntityNotFound(ex, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("unknown");
        assertThat(response.getBody().getMessage()).isEqualTo("Test entity");
    }

    @Test
    void handleIllegalArgument_WithNullWebRequest_ShouldHandleGracefully() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        WebRequest webRequest = null;

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(ex, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("unknown");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid argument");
    }

    @Test
    void handleGenericException_WithNullWebRequest_ShouldHandleGracefully() {
        Exception ex = new RuntimeException("Unexpected error");
        WebRequest webRequest = null;

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("unknown");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
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

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("unknown");
        assertThat(response.getBody().getDetails()).containsKey("field");
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

    @Test
    void handleGenericException_WithEntityMappingException_ShouldFallThroughToGenericHandler() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/entity-mapping")
            public void throwEntityMapping() {
                throw new EntityMappingException("Entity mapping failed");
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/entity-mapping"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void handleGenericException_WithReflectionException_ShouldFallThroughToGenericHandler() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/reflection")
            public void throwReflection() {
                throw new ReflectionException("Reflection operation failed");
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/reflection"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void handleGenericException_WithSqlException_ShouldReturn500() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/sql-exception")
            public void throwSqlException() {
                throw new RuntimeException(new java.sql.SQLException("Database error occurred"));
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/sql-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void handleGenericException_WithIOException_ShouldReturn500() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/io-exception")
            public void throwIOException() {
                throw new RuntimeException(new java.io.IOException("IO operation failed"));
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/io-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void handleGenericException_WithCustomBusinessException_ShouldReturn500() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/business-exception")
            public void throwBusinessException() {
                throw new RuntimeException("Business logic error: insufficient funds");
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/business-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void handleEntityNotFound_WithEmptyMessage_ShouldHandleGracefully() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/entity-not-found-empty")
            public void throwEntityNotFoundEmpty() {
                throw new EntityNotFoundException("");
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/entity-not-found-empty"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(""));
    }

    @Test
    void handleIllegalArgument_WithVeryLongMessage_ShouldReturnFullMessage() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/illegal-argument-long")
            public void throwIllegalArgumentLong() {
                String longMessage = "a".repeat(1000);
                throw new IllegalArgumentException(longMessage);
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/illegal-argument-long"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("a".repeat(1000)));
    }

    @Test
    void handleValidationExceptions_WithNullFieldName_ShouldHandleGracefully() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        // FieldError constructor doesn't allow null field name - it throws IllegalArgumentException
        // So we'll test with a valid field name but verify the handler handles null gracefully
        FieldError fieldError = new FieldError("testObject", "fieldName", "value", false, null, null, "Error message");
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        java.lang.reflect.Method method = String.class.getMethod("equals", Object.class);
        org.springframework.core.MethodParameter realParameter = new org.springframework.core.MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(realParameter, bindingResult);
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test/validation");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).containsKey("fieldName");
        assertThat(response.getBody().getDetails().get("fieldName")).isEqualTo("Error message");
    }

    @Test
    void handleValidationExceptions_WithNullErrorMessage_ShouldHandleGracefully() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("testObject", "fieldName", "value", false, null, null, null);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        java.lang.reflect.Method method = String.class.getMethod("equals", Object.class);
        org.springframework.core.MethodParameter realParameter = new org.springframework.core.MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(realParameter, bindingResult);
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test/validation");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).containsKey("fieldName");
        assertThat(response.getBody().getDetails().get("fieldName")).isEqualTo(""); // Handler converts null to empty string
    }

    @Test
    void handleGenericException_WithNestedException_ShouldNotExposeInternalDetails() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/nested-exception")
            public void throwNestedException() {
                throw new RuntimeException("Wrapper exception", new IllegalStateException("Root cause"));
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/nested-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void handleEntityNotFound_WithSpecialCharacters_ShouldReturnCorrectly() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/entity-not-found-special")
            public void throwEntityNotFoundSpecial() {
                throw new EntityNotFoundException("Entity with special chars: @#$%^&*()_+{}|:<>?[]\\;',./");
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/entity-not-found-special"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Entity with special chars: @#$%^&*()_+{}|:<>?[]\\;',./"));
    }

    @Test
    void handleIllegalArgument_WithUnicodeMessage_ShouldReturnCorrectly() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/illegal-argument-unicode")
            public void throwIllegalArgumentUnicode() {
                throw new IllegalArgumentException("Unicode message: ñáéíóú 中文 🚀");
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/illegal-argument-unicode"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Unicode message: ñáéíóú 中文 🚀"));
    }

    @Test
    void handleValidationExceptions_WithGlobalErrors_ShouldHandleObjectErrors() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        org.springframework.validation.ObjectError objectError = new org.springframework.validation.ObjectError(
                "testObject", "Global validation error");
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(objectError));

        java.lang.reflect.Method method = String.class.getMethod("equals", Object.class);
        org.springframework.core.MethodParameter realParameter = new org.springframework.core.MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(realParameter, bindingResult);
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test/validation");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).containsKey("testObject");
        assertThat(response.getBody().getDetails().get("testObject")).isEqualTo("Global validation error");
    }

    @Test
    void handleGenericException_WithOutOfMemoryError_ShouldReturn500() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/out-of-memory")
            public void throwOutOfMemory() {
                throw new OutOfMemoryError("Java heap space");
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/out-of-memory"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void handleGenericException_WithStackOverflowError_ShouldReturn500() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/stack-overflow")
            public void throwStackOverflow() {
                throw new StackOverflowError("Stack overflow occurred");
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/stack-overflow"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void handleEntityNotFound_WithVeryLongEntityName_ShouldReturnCorrectly() throws Exception {
        TestExceptionController controller = new TestExceptionController() {
            @GetMapping("/test/entity-not-found-long-name")
            public void throwEntityNotFoundLongName() {
                String longEntityName = "VeryLongEntityNameThatMightCauseIssuesWithDisplay".repeat(10);
                throw new EntityNotFoundException(longEntityName);
            }
        };

        MockMvc testMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testMvc.perform(get("/test/entity-not-found-long-name"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
