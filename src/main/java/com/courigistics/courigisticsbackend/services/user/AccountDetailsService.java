package com.courigistics.courigisticsbackend.services.user;

import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.repositories.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AccountDetailsService implements UserDetailsService {

    private AccountRepository accountRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading user details for: {}", usernameOrEmail);

        // We try to find the username first then by email
        Account account = accountRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> accountRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "user not found with username or email: " + usernameOrEmail
                        ))
                );

        log.debug("User found: {}", account.getUsername());
        return account;
    }
}
