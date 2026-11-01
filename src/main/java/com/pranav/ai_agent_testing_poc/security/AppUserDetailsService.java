
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

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final Map<String, Account> accounts;

    public AppUserDetailsService(PasswordEncoder passwordEncoder) {

        Account alice = new Account(
                DemoUserIds.ALICE,
                "alice",
                passwordEncoder.encode("alice123"),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Account bob = new Account(
                DemoUserIds.BOB,
                "bob",
                passwordEncoder.encode("bob123"),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Account admin = new Account(
                DemoUserIds.ADMIN,
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
            throw new UsernameNotFoundException(
                    "Username is required."
            );
        }

        Account account = accounts.get(
                username.trim().toLowerCase(Locale.ROOT)
        );

        if (account == null) {
            throw new UsernameNotFoundException(
                    "User not found."
            );
        }

        return new AppUserDetails(
                account.userId(),
                account.username(),
                account.passwordHash(),
                account.authorities()
        );
    }

    private record Account(
            java.util.UUID userId,
            String username,
            String passwordHash,
            List<GrantedAuthority> authorities
    ) {
        private Account {
            authorities = List.copyOf(authorities);
        }
    }
}
