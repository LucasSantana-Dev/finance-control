package com.finance_control.unit.shared.service;

import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.shared.service.BaseService;
import com.finance_control.shared.context.UserContext;
import com.finance_control.users.dto.UserDTO;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

    private static final String TEST_PASSWORD = "password123";

    @Mock
    private BaseRepository<User, Long> repository;

    private TestBaseService service;

    @BeforeEach
    void setUp() {
        service = new TestBaseService(repository);
        UserContext.setCurrentUserId(1L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void findAllShouldApplySortingWhenSortByIsProvided() {
        // Given
        String search = "test";
        String sortBy = "email";
        String sortDirection = "desc";
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("alice@example.com");
        user1.setPassword(TEST_PASSWORD);
        user1.setIsActive(true);

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("bob@example.com");
        user2.setPassword(TEST_PASSWORD);
        user2.setIsActive(true);

        List<User> users = List.of(user1, user2);
        Page<User> userPage = new PageImpl<>(users, pageable, 2);

        when(repository.findAll(eq(search), any(Pageable.class))).thenReturn(userPage);

        // When
        Page<UserDTO> result = service.findAll(search, null, sortBy, sortDirection, pageable);

        // Then
        verify(repository).findAll(eq(search), any(Pageable.class));
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findAllShouldNotApplySortingWhenSortByIsNull() {
        // Given
        String search = "test";
        String sortBy = null;
        String sortDirection = "asc";
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword(TEST_PASSWORD);
        user.setIsActive(true);

        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);

        when(repository.findAll(eq(search), eq(pageable))).thenReturn(userPage);

        // When
        Page<UserDTO> result = service.findAll(search, null, sortBy, sortDirection, pageable);

        // Then
        verify(repository).findAll(eq(search), eq(pageable));
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAllShouldNotApplySortingWhenSortByIsEmpty() {
        // Given
        String search = "test";
        String sortBy = "";
        String sortDirection = "asc";
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword(TEST_PASSWORD);
        user.setIsActive(true);

        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);

        when(repository.findAll(eq(search), eq(pageable))).thenReturn(userPage);

        // When
        Page<UserDTO> result = service.findAll(search, null, sortBy, sortDirection, pageable);

        // Then
        verify(repository).findAll(eq(search), eq(pageable));
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void create_WithValidData_ShouldCreateEntity() {
        // Given
        UserDTO createDTO = new UserDTO();
        createDTO.setEmail("test@example.com");
        createDTO.setPassword(TEST_PASSWORD);
        createDTO.setIsActive(true);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setPassword(TEST_PASSWORD);
        savedUser.setIsActive(true);

        when(repository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDTO result = service.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(repository).save(any(User.class));
    }

    @Test
    void findById_WithExistingId_ShouldReturnEntity() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword(TEST_PASSWORD);
        user.setIsActive(true);

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        // When
        Optional<UserDTO> result = service.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(repository).findById(1L);
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<UserDTO> result = service.findById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(repository).findById(999L);
    }

    @Test
    void update_WithValidData_ShouldUpdateEntity() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");
        existingUser.setPassword(TEST_PASSWORD);
        existingUser.setIsActive(true);

        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("new@example.com");
        updateDTO.setIsActive(false);

        when(repository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(repository.save(any(User.class))).thenReturn(existingUser);

        // When
        UserDTO result = service.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getIsActive()).isEqualTo(false);
        verify(repository).findById(1L);
        verify(repository).save(any(User.class));
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        // Given
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("new@example.com");

        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.update(999L, updateDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
        verify(repository).findById(999L);
    }

    @Test
    void delete_WithExistingId_ShouldDeleteEntity() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword(TEST_PASSWORD);
        user.setIsActive(true);

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        // When
        service.delete(1L);

        // Then
        verify(repository).findById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void delete_WithNonExistingId_ShouldThrowException() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
        verify(repository).findById(999L);
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // Given
        when(repository.existsById(1L)).thenReturn(true);

        // When
        boolean result = service.existsById(1L);

        // Then
        assertThat(result).isTrue();
        verify(repository).existsById(1L);
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        // Given
        when(repository.existsById(999L)).thenReturn(false);

        // When
        boolean result = service.existsById(999L);

        // Then
        assertThat(result).isFalse();
        verify(repository).existsById(999L);
    }

    @Test
    void count_WithSearchAndFilters_ShouldReturnCount() {
        // Given
        String search = "test";
        Map<String, Object> filters = new HashMap<>();
        filters.put("isActive", true);

        when(repository.count(any(Specification.class))).thenReturn(5L);

        // When
        long result = service.count(search, filters);

        // Then
        assertThat(result).isEqualTo(5L);
        verify(repository).count(any(Specification.class));
    }

    @Test
    void findAll_WithSearchAndFilters_ShouldReturnResults() {
        // Given
        String search = "test";
        Map<String, Object> filters = new HashMap<>();
        filters.put("isActive", true);
        String sortBy = "email";
        String sortDirection = "asc";

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword(TEST_PASSWORD);
        user.setIsActive(true);

        Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // When
        Page<UserDTO> result = service.findAll(search, filters, sortBy, sortDirection, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_WithNoFilters_ShouldUseSearchOnly() {
        // Given
        String search = "test";
        Map<String, Object> filters = new HashMap<>();
        String sortBy = null;
        String sortDirection = "asc";
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword(TEST_PASSWORD);
        user.setIsActive(true);

        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
        when(repository.findAll(eq(search), any(Pageable.class))).thenReturn(userPage);

        // When
        Page<UserDTO> result = service.findAll(search, filters, sortBy, sortDirection, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findAll(eq(search), any(Pageable.class));
    }

    @Test
    void findAll_WithFilters_ShouldUseSpecifications() {
        // Given
        String search = "test";
        Map<String, Object> filters = new HashMap<>();
        filters.put("isActive", true);
        String sortBy = "email";
        String sortDirection = "asc";
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword(TEST_PASSWORD);
        user.setIsActive(true);

        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // When
        Page<UserDTO> result = service.findAll(search, filters, sortBy, sortDirection, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    // Test implementation of BaseService
    private static class TestBaseService extends BaseService<User, Long, UserDTO> {

        public TestBaseService(BaseRepository<User, Long> repository) {
            super(repository);
        }

        @Override
        protected User mapToEntity(UserDTO createDTO) {
            User user = new User();
            user.setEmail(createDTO.getEmail());
            user.setPassword(createDTO.getPassword());
            user.setIsActive(createDTO.getIsActive());
            return user;
        }

        @Override
        protected void updateEntityFromDTO(User entity, UserDTO updateDTO) {
            if (updateDTO.getEmail() != null) {
                entity.setEmail(updateDTO.getEmail());
            }
            if (updateDTO.getPassword() != null) {
                entity.setPassword(updateDTO.getPassword());
            }
            if (updateDTO.getIsActive() != null) {
                entity.setIsActive(updateDTO.getIsActive());
            }
        }

        @Override
        protected UserDTO mapToResponseDTO(User entity) {
            UserDTO dto = new UserDTO();
            dto.setId(entity.getId());
            dto.setEmail(entity.getEmail());
            dto.setPassword(entity.getPassword());
            dto.setIsActive(entity.getIsActive());
            return dto;
        }
    }
}
