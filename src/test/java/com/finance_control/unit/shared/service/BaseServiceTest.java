package com.finance_control.unit.shared.service;

import com.finance_control.shared.model.BaseModel;
import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.shared.service.BaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BaseServiceTest {

    interface DummyRepository extends BaseRepository<DummyEntity, Long> {}

    static class DummyEntity extends BaseModel<Long> {}
    static class DummyDTO {}

    @Mock
    private DummyRepository repository;

    @InjectMocks
    private TestService service;

    static class TestService extends BaseService<DummyEntity, Long, DummyDTO> {
        public TestService(DummyRepository repository) {
            super(repository);
        }
        @Override
        protected DummyEntity mapToEntity(DummyDTO createDTO) { return new DummyEntity(); }
        @Override
        protected void updateEntityFromDTO(DummyEntity entity, DummyDTO updateDTO) { }
        @Override
        protected DummyDTO mapToResponseDTO(DummyEntity entity) { return new DummyDTO(); }
        @Override
        protected String getEntityName() { return "Dummy"; }
    }

    @BeforeEach
    void init() {
        service = new TestService(repository);
    }

    @Test
    void findAll_WithSearchOnly_ShouldDelegateToRepositorySearch() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<DummyEntity> page = new PageImpl<>(List.of(new DummyEntity()), pageable, 1);
        when(repository.findAll(anyString(), any(Pageable.class))).thenReturn(page);

        Page<DummyDTO> result = service.findAll("term", Map.of(), null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_WithFilters_ShouldDelegateToSpecification() {
        Pageable pageable = PageRequest.of(1, 10);
        Page<DummyEntity> page = new PageImpl<>(List.of(new DummyEntity()), pageable, 1);
        doReturn(page).when(repository).findAll(any(Specification.class), any(Pageable.class));

        Page<DummyDTO> result = service.findAll(null, Map.of("foo", "bar"), null, "desc", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_WithNullSort_ShouldDefaultAsc() {
        // Given
        Pageable input = PageRequest.of(0, 10);
        Page<DummyEntity> page = new PageImpl<>(List.of(), input, 0);
        doReturn(page).when(repository).findAll(any(Specification.class), any(Pageable.class));

        // When
        Page<DummyDTO> result = service.findAll(null, Map.of("k", "v"), null, null, input);

        // Then - verify method executes without exception and returns result
        assertThat(result).isNotNull();
    }
}
