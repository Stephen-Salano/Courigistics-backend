package com.courigistics.courigisticsbackend.services.auth;

import com.courigistics.courigisticsbackend.config.security.JwtService;
import com.courigistics.courigisticsbackend.dto.requests.auth.LoginRequest;
import com.courigistics.courigisticsbackend.dto.responses.auth.AuthResponse;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.RefreshToken;
import com.courigistics.courigisticsbackend.entities.enums.AccountType;
import com.courigistics.courigisticsbackend.exceptions.BadRequestException;
import com.courigistics.courigisticsbackend.repositories.AccountRepository;
import com.courigistics.courigisticsbackend.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Admin login attempt: {}", request.usernameOrEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.usernameOrEmail(),
                        request.password()
                )
        );

        Account account = (Account) authentication.getPrincipal();

        if (account.getAccountType() != AccountType.ADMIN) {
            throw new BadRequestException("Invalid account type for admin login");
        }

        String accessToken = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken(account);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .account(account)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                .invalidated(false)
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        account.setLastLogin(LocalDateTime.now());
        accountRepository.save(account);

        log.info("Admin login successful: {}", account.getUsername());

        return AuthResponse.of(
                accessToken, refreshToken,
                jwtService.getAccessTokenExpiration() / 1000,
                account.getUsername(),
                account.getEmail(), 
                account.getAccountType().name()
        );
    }

    @Override
    @Transactional
    public void logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Account account) {
            log.info("Admin logout: {}", account.getUsername());
            refreshTokenRepository.invalidateAllByAccount(account);
        }
    }
}
