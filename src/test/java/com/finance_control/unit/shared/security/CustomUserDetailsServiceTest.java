package com.finance_control.unit.shared.security;

import com.finance_control.shared.security.CustomUserDetails;
import com.finance_control.shared.security.CustomUserDetailsService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
    }

    @Test
    @DisplayName("loadUserByUsername_WithValidUserId_ShouldReturnUserDetails")
    void loadUserByUsername_WithValidUserId_ShouldReturnUserDetails() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("1");

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails customUserDetails = (CustomUserDetails) result;
        assertThat(customUserDetails.getUser().getId()).isEqualTo(1L);
        assertThat(customUserDetails.getUser().getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("loadUserByUsername_WithInvalidUserId_ShouldThrowUsernameNotFoundException")
    void loadUserByUsername_WithInvalidUserId_ShouldThrowUsernameNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("999"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("loadUserByUsername_WithInvalidFormat_ShouldThrowUsernameNotFoundException")
    void loadUserByUsername_WithInvalidFormat_ShouldThrowUsernameNotFoundException() {
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("not-a-number"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Invalid user ID format: not-a-number");

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("loadUserByUsername_WithNullUsername_ShouldThrowUsernameNotFoundException")
    void loadUserByUsername_WithNullUsername_ShouldThrowUsernameNotFoundException() {
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Invalid user ID format");

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("loadUserByUsername_WithEmptyString_ShouldThrowUsernameNotFoundException")
    void loadUserByUsername_WithEmptyString_ShouldThrowUsernameNotFoundException() {
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Invalid user ID format");

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("loadUserByUsername_WithZeroId_ShouldThrowUsernameNotFoundException")
    void loadUserByUsername_WithZeroId_ShouldThrowUsernameNotFoundException() {
        when(userRepository.findById(0L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("0"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with ID: 0");

        verify(userRepository).findById(0L);
    }

    @Test
    @DisplayName("loadUserByUsername_WithNegativeId_ShouldThrowUsernameNotFoundException")
    void loadUserByUsername_WithNegativeId_ShouldThrowUsernameNotFoundException() {
        when(userRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("-1"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with ID: -1");

        verify(userRepository).findById(-1L);
    }
}
