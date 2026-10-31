
package com.pranav.ai_agent_testing_poc.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final Map<String, Account> accounts;

    public AppUserDetailsService(PasswordEncoder passwordEncoder) {

        Account alice = new Account(
                UUID.fromString("11111111-1111-4111-8111-111111111111"),
                "alice",
                passwordEncoder.encode("alice123"),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Account bob = new Account(
                UUID.fromString("22222222-2222-4222-8222-222222222222"),
                "bob",
                passwordEncoder.encode("bob123"),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Account admin = new Account(
                UUID.fromString("33333333-3333-4333-8333-333333333333"),
                "admin",
                passwordEncoder.encode("admin123"),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        this.accounts = Map.of(
                "alice", alice,
                "bob", bob,
                "admin", admin
        );
    }

    @Override
    public AppUserDetails loadUserByUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username is required.");
        }

        Account account = accounts.get(
                username.trim().toLowerCase(Locale.ROOT)
        );

        if (account == null) {
            throw new UsernameNotFoundException(
                    "User not found."
            );
        }

        // Return a fresh principal for every lookup.
        return new AppUserDetails(
                account.userId(),
                account.username(),
                account.passwordHash(),
                account.authorities()
        );
    }

    private record Account(
            UUID userId,
            String username,
            String passwordHash,
            List<GrantedAuthority> authorities
    ) {
        private Account {
            authorities = List.copyOf(authorities);
        }
    }
}
