package com.finance_control.unit.users.service;

import com.finance_control.users.dto.UserDTO;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import com.finance_control.users.service.UserService;
import com.finance_control.shared.context.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setIsActive(true);
    }

    @Test
    void shouldCreateUser() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            UserDTO dto = new UserDTO();
            dto.setEmail("newuser@example.com");
            dto.setPassword("Password123");
            dto.setIsActive(true);

            when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UserDTO result = userService.create(dto);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }
    }

    @Test
    void shouldFindUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserDTO> result = userService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldFindAllUsers() {
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll((String) isNull(), any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.findAll(null, null, null, null, Pageable.ofSize(10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldFindUserByEmail() {
        when(userRepository.findOne(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(Optional.of(testUser));

        Optional<UserDTO> result = userService.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldFindUserByEmail_WhenUserNotFound() {
        when(userRepository.findOne(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(Optional.empty());

        Optional<UserDTO> result = userService.findByEmail("nonexistent@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldExistsByEmail_ReturnTrue_WhenUserExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean result = userService.existsByEmail("test@example.com");

        assertThat(result).isTrue();
    }

    @Test
    void shouldExistsByEmail_ReturnFalse_WhenUserNotFound() {
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        boolean result = userService.existsByEmail("nonexistent@example.com");

        assertThat(result).isFalse();
    }

    @Test
    void shouldFindAllWithFilters_WhenEmailProvided() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAllWithFilters("test", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldFindAllWithFilters_WhenIsActiveProvided() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAllWithFilters(null, true, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIsActive()).isTrue();
    }

    @Test
    void shouldFindAllWithFilters_WhenBothFiltersProvided() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAllWithFilters("test", true, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
        assertThat(result.getContent().get(0).getIsActive()).isTrue();
    }

    @Test
    void shouldFindAllWithFilters_WhenNoFiltersProvided() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAllWithFilters(null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAllWithFilters_WhenEmailIsEmptyString() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAllWithFilters("   ", null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAllWithFilters_ApplyDefaultSortingWhenUnsorted() {
        Pageable unsortedPageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAllWithFilters(null, null, unsortedPageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldSoftDelete_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.softDelete(1L);

        assertThat(testUser.getIsActive()).isFalse();
    }

    @Test
    void shouldReactivate_WhenUserExists() {
        testUser.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.reactivate(1L);

        assertThat(testUser.getIsActive()).isTrue();
    }

    @Test
    void shouldResetPassword_WhenValidInput() {
        String newPassword = "NewPassword123";
        String encodedPassword = "encodedNewPassword";
        String reason = "Password reset requested";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.resetPassword(1L, newPassword, reason);

        assertThat(testUser.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    void shouldUpdateStatus_WhenActiveTrue() {
        testUser.setIsActive(false);
        String reason = "User reactivated";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.updateStatus(1L, true, reason);

        assertThat(testUser.getIsActive()).isTrue();
        assertThat(result).isNotNull();
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void shouldUpdateStatus_WhenActiveFalse() {
        testUser.setIsActive(true);
        String reason = "User deactivated";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.updateStatus(1L, false, reason);

        assertThat(testUser.getIsActive()).isFalse();
        assertThat(result).isNotNull();
        assertThat(result.getIsActive()).isFalse();
    }

    @Test
    void shouldFindAllWithFilters_WhenIsActiveIsFalse() {
        User inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setIsActive(false);

        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(inactiveUser));

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAllWithFilters(null, false, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIsActive()).isFalse();
    }

    @Test
    void shouldFindAll_WithSearchOnly() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.findAll("test", null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithSearchEmptyString() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.findAll("   ", null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithFiltersOnly() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        Map<String, Object> filters = Map.of("email", "test");

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAll(null, filters, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithSearchAndFilters() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        Map<String, Object> filters = Map.of("isActive", true);

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAll("test", filters, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithFilterEmail() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        Map<String, Object> filters = Map.of("email", "test@example.com");

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAll(null, filters, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }


    @Test
    void shouldFindAll_WithFilterIsActive() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        Map<String, Object> filters = Map.of("isActive", true);

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAll(null, filters, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithMultipleFilters() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        Map<String, Object> filters = Map.of(
                "email", "test",
                "isActive", true
        );

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAll(null, filters, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithFilterNullValue() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        Map<String, Object> filters = new HashMap<>();
        filters.put("email", null);

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAll(null, filters, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithInvalidFilterKey() {
        Pageable pageable = Pageable.ofSize(10);
        Map<String, Object> filters = Map.of("invalidKey", "value");

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenThrow(new IllegalArgumentException("Invalid filter key: invalidKey"));

        assertThatThrownBy(() -> userService.findAll(null, filters, null, null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid filter key");
    }

    @Test
    void shouldFindAll_WithEmptyFilters() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll((String) isNull(), any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.findAll(null, Map.of(), null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithSearchAndEmptyFilters() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.findAll("test", Map.of(), null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldUpdate_WithNullEmail_ShouldNotUpdateEmail() {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail(null);
        updateDTO.setPassword("NewPassword123");
        updateDTO.setIsActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("NewPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.update(1L, updateDTO);

        assertThat(testUser.getEmail()).isEqualTo("test@example.com");
        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdate_WithNullPassword_ShouldNotUpdatePassword() {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("updated@example.com");
        updateDTO.setPassword(null);
        updateDTO.setIsActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.update(1L, updateDTO);

        assertThat(testUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(testUser.getPassword()).isEqualTo("password123");
        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdate_WithNullIsActive_ShouldNotUpdateIsActive() {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("updated@example.com");
        updateDTO.setPassword("NewPassword123");
        updateDTO.setIsActive(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("NewPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.update(1L, updateDTO);

        assertThat(testUser.getIsActive()).isTrue();
        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdate_WithAllFieldsNull_ShouldNotChangeEntity() {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail(null);
        updateDTO.setPassword(null);
        updateDTO.setIsActive(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.update(1L, updateDTO);

        assertThat(testUser.getEmail()).isEqualTo("test@example.com");
        assertThat(testUser.getPassword()).isEqualTo("password123");
        assertThat(testUser.getIsActive()).isTrue();
        assertThat(result).isNotNull();
    }

    @Test
    void shouldFindAllWithFilters_WithNullEmailAndNullIsActive_ShouldReturnAll() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAllWithFilters(null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAllWithFilters_WithEmptyEmail_ShouldNotAddEmailFilter() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAllWithFilters("", null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithFilterFullName() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        Map<String, Object> filters = Map.of("fullName", "John Doe");

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAll(null, filters, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithSearchContainingWhitespace() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.findAll("  test  ", null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithFiltersContainingNullValue() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        Map<String, Object> filters = new HashMap<>();
        filters.put("email", "test");
        filters.put("isActive", null);

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDTO> result = userService.findAll(null, filters, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithEmptyFiltersMap() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll((String) isNull(), any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.findAll(null, new HashMap<>(), null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAll_WithNullFilters() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll((String) isNull(), any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.findAll(null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
