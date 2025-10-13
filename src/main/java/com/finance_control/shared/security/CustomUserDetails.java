package com.finance_control.shared.security;

import com.finance_control.users.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation that wraps the actual User entity.
 * This allows @AuthenticationPrincipal to inject the actual User object.
 */
public class CustomUserDetails implements UserDetails {
    
    private final User user;
    
    public CustomUserDetails(User user) {
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("USER"));
    }
    
    @Override
    public String getPassword() {
        return ""; // Password is not used for JWT authentication
    }
    
    @Override
    public String getUsername() {
        return user.getId().toString();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }
}
