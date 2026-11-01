
package com.pranav.ai_agent_testing_poc.security;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AppUserDetails
        implements UserDetails, CredentialsContainer {

    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private final String username;
    private String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserDetails(
            UUID userId,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities) {

        this.userId = Objects.requireNonNull(userId);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
        this.authorities = List.copyOf(authorities);
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isAdmin() {
    return authorities.stream()
            .anyMatch(authority ->
                    "ROLE_ADMIN".equals(authority.getAuthority())
            );
}

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
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
        return true;
    }
}
