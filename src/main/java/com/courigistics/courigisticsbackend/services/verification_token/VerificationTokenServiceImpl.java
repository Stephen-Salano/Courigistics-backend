package com.courigistics.courigisticsbackend.services.verification_token;

import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.VerificationToken;
import com.courigistics.courigisticsbackend.entities.enums.TokenType;
import com.courigistics.courigisticsbackend.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${app.verification-token.expiration-minutes}")
    private int expirationInMinutes;

    @Value("${app.verification-token.token-length}")
    private int tokenLength;

    // to generate cryptographically strong random numbers
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public VerificationToken createToken(Account account, TokenType tokenType) {
        // First we invalidate any existing tokens of the same type for this account
        invalidateTokens(account, tokenType);

        // create a new token with a secure random value and exoiration time
        VerificationToken verificationToken = VerificationToken.builder()
                .account(account)
                .token(generateSecureToken())
                .tokenType(tokenType.toString())
                .expiryDate(LocalDateTime.now().plusMinutes(expirationInMinutes))
                .build();
        return verificationTokenRepository.save(verificationToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VerificationToken> validateToken(String token, TokenType expectedType) {
        return verificationTokenRepository.findByToken(token)
                // Condition 1: The token must not be expired
                .filter(verificationToken -> verificationToken.getExpiryDate().isAfter(LocalDateTime.now()))
                // Condition 2: The token must match the expected type
                .filter(verificationToken -> verificationToken.getTokenType().equalsIgnoreCase(expectedType.toString()));
    }

    @Override
    public void invalidateTokens(Account account, TokenType tokenType) {
        // First we find any existing token for this account and type
        Optional<VerificationToken> existingToken = verificationTokenRepository
                .findByAccountAndTokenTypes(account, tokenType);

        // If found, delete
        existingToken.ifPresent(verificationTokenRepository::delete); // '::' is a pointer to the repository's delete method
    }

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    @Override
    public void deleteToken(VerificationToken token) {
        verificationTokenRepository.delete(token);
    }

    private String generateSecureToken() {
        // Using secure random ensures tokens aren't predictable

        //create a byte array for the random bytes
        byte[] randomBytes = new byte[tokenLength];
        // Fill with secure random values
        secureRandom.nextBytes(randomBytes);;
        // Encoding using base64URL-safe encoding and return as String
        return Base64 // This means we embed this token directly in links
                .getUrlEncoder()  // choose URL and filename safe Base64 variant
                .withoutPadding() // Drop the trailing "=" characters
                .encodeToString(randomBytes); // performs the conversion and returns your new token
    }
}
