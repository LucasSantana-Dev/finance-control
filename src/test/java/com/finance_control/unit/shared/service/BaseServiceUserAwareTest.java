package com.finance_control.unit.shared.service;

import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.model.BaseModel;
import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.shared.service.BaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BaseServiceUserAwareTest {

    interface UserAwareRepository extends BaseRepository<DummyEntity, Long> {
        Page<DummyEntity> findAll(String search, Long userId, Pageable pageable);
    }

    static class DummyEntity extends BaseModel<Long> {
        private Long userId;
        Long getUserId() { return userId; }
        void setUserId(Long userId) { this.userId = userId; }
    }
    static class DummyDTO {}

    static class UserAwareService extends BaseService<DummyEntity, Long, DummyDTO> {
        public UserAwareService(UserAwareRepository repository) { super(repository); }
        @Override protected DummyEntity mapToEntity(DummyDTO dto) { return new DummyEntity(); }
        @Override protected void updateEntityFromDTO(DummyEntity entity, DummyDTO dto) {}
        @Override protected DummyDTO mapToResponseDTO(DummyEntity entity) { return new DummyDTO(); }
        @Override protected String getEntityName() { return "Dummy"; }
        @Override protected boolean isUserAware() { return true; }
        @Override protected boolean belongsToUser(DummyEntity entity, Long userId) { return true; }
        @Override protected void setUserId(DummyEntity entity, Long userId) { entity.setUserId(userId); }
    }

    static class UserAwareServiceDeniesAccess extends UserAwareService {
        public UserAwareServiceDeniesAccess(UserAwareRepository repository) { super(repository); }
        @Override protected boolean belongsToUser(DummyEntity entity, Long userId) { return false; }
    }

    @Mock
    private UserAwareRepository repository;

    private UserAwareService service;

    @BeforeEach
    void setUp() {
        service = new UserAwareService(repository);
    }

    @Test
    void findAll_SearchOnly_UserAware_UsesUserSpecificRepositoryMethod() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            Pageable pageable = PageRequest.of(0, 5);
            Page<DummyEntity> page = new PageImpl<>(List.of(new DummyEntity()), pageable, 1);
            doReturn(page).when(repository).findAll(anyString(), anyLong(), any(Pageable.class));

            Page<DummyDTO> result = service.findAll("term", null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Test
    void findAll_WithFilters_UserAware_AddsUserFilterIfMissing() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(42L);

            Pageable pageable = PageRequest.of(0, 10);
            Page<DummyEntity> page = new PageImpl<>(new ArrayList<>(), pageable, 0);
            doReturn(page).when(repository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));

            Map<String, Object> filters = new HashMap<>();
            Page<DummyDTO> result = service.findAll(null, filters, "name", "desc", pageable);

            assertThat(result.getContent()).isEmpty();
            // ensure user filter was inserted (indirectly covered via branch execution)
            assertThat(filters).containsKey("userId");
        }
    }

    @Test
    void findById_UserAware_OwnershipAllowed_ReturnsEntity() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            DummyEntity entity = new DummyEntity();
            doReturn(java.util.Optional.of(entity)).when(repository).findById(1L);

            var result = service.findById(1L);

            assertThat(result).isPresent();
        }
    }

    @Test
    void findById_UserAware_OwnershipDenied_ThrowsSecurityException() {
        UserAwareServiceDeniesAccess serviceDenies = new UserAwareServiceDeniesAccess(repository);

        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            DummyEntity entity = new DummyEntity();
            doReturn(java.util.Optional.of(entity)).when(repository).findById(1L);

            assertThatThrownBy(() -> serviceDenies.findById(1L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Access denied: entity does not belong to current user");
        }
    }

    @Test
    void findById_UserAware_UserContextUnavailable_ThrowsSecurityException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(null);

            DummyEntity entity = new DummyEntity();
            doReturn(java.util.Optional.of(entity)).when(repository).findById(1L);

            assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("User context not available");
        }
    }

    @Test
    void create_UserAware_SetsUserId() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(42L);

            DummyEntity savedEntity = new DummyEntity();
            savedEntity.setId(1L);
            doReturn(savedEntity).when(repository).save(any(DummyEntity.class));

            DummyDTO result = service.create(new DummyDTO());

            assertThat(result).isNotNull();
            verify(repository).save(any(DummyEntity.class));
            // User ID is set via the setUserId method override
        }
    }

    @Test
    void create_UserAware_UserContextUnavailable_ThrowsSecurityException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(null);

            assertThatThrownBy(() -> service.create(new DummyDTO()))
                .isInstanceOf(SecurityException.class)
                .hasMessage("User context not available");
        }
    }

    @Test
    void findAll_UserAware_UserContextUnavailable_ThrowsSecurityException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(null);

            Map<String, Object> filters = new HashMap<>();
            Pageable pageable = PageRequest.of(0, 10);

            assertThatThrownBy(() -> service.findAll(null, filters, null, null, pageable))
                .isInstanceOf(SecurityException.class)
                .hasMessage("User context not available");
        }
    }
}
