package com.finance_control.unit.shared.controller;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.shared.model.BaseModel;
import com.finance_control.shared.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BaseControllerTest {

    @Mock
    private BaseService<DummyEntity, Long, DummyDTO> service;

    private MockMvc mockMvc;

    private TestController controller;

    @BeforeEach
    void setUp() {
        controller = new TestController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void findAll_ShouldExtractFilters_AndCallService() throws Exception {
        Page<DummyDTO> empty = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(service.findAll(any(), any(), any(), any(), any(Pageable.class))).thenReturn(empty);

        mockMvc.perform(get("/")
                        .param("search", "term")
                        .param("sortBy", "email")
                        .param("sortDirection", "desc")
                        .param("page", "0")
                        .param("size", "10")
                        .param("foo", "bar")
                        .param("active", "true")
                        .param("age", "42")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> filtersCaptor = ArgumentCaptor.forClass(Map.class);
        verify(service).findAll(eq("term"), filtersCaptor.capture(), eq("email"), eq("desc"), any(Pageable.class));

        Map<String, Object> filters = filtersCaptor.getValue();
        assertThat(filters).containsEntry("foo", "bar");
        assertThat(filters).containsEntry("active", true);
        assertThat(filters).containsEntry("age", 42L);
        assertThat(filters).doesNotContainKeys("search", "sortBy", "sortDirection", "page", "size", "sort");
    }

    @RestController
    private static class TestController extends BaseController<DummyEntity, Long, DummyDTO> {
        protected TestController(BaseService<DummyEntity, Long, DummyDTO> service) {
            super(service);
        }

        @Override
        @GetMapping
        public ResponseEntity<Page<DummyDTO>> findAll(
                @RequestParam(required = false) String search,
                @RequestParam(required = false) String sortBy,
                @RequestParam(required = false, defaultValue = "asc") String sortDirection,
                Pageable pageable,
                HttpServletRequest request) {
            return super.findAll(search, sortBy, sortDirection, pageable, request);
        }
    }

    private static class DummyDTO {
        // minimal DTO for generic parameter satisfaction
    }

    private static class DummyEntity extends BaseModel<Long> {
        // minimal entity for generic parameter satisfaction
    }
}
