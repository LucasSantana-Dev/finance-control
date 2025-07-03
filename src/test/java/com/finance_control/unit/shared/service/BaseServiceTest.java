package com.finance_control.unit.shared.service;

import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.shared.service.BaseService;
import com.finance_control.users.dto.UserDTO;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    }

    @Test
    void findAllShouldApplySortingWhenSortByIsProvided() {
        // Given
        String search = "test";
        String sortBy = "fullName";
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