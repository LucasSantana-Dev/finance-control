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
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setIsActive(true);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setIsActive(true);
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
}
