package com.courigistics.courigisticsbackend.services.verification_token;

import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.VerificationToken;
import com.courigistics.courigisticsbackend.entities.enums.TokenType;

import java.util.Optional;

public interface VerificationTokenService {

    /**
     * Creates a verificaion token for a specific account
     * @param account the account to create the token for
     * @param tokenType the type of token to create (VERIFICATION, PASSWORD_RESET)
     * @return the created verification token entity
     */
    VerificationToken createToken(Account account, TokenType tokenType);

    /**
     * Verifies is a token is valid and not expired
     * @param token The token string to validate
     * @param expectedType Which type the token should be
     * @return the verification token if valid, empty if not found or expired
     */
    Optional<VerificationToken> validateToken(String token, TokenType expectedType);

    /**
     * Invalidates all tokens of a specific type for an account
     * @param account the account whose tokens should be invalidated
     * @param tokenType the type of tokens to invalidate
     */
    void invalidateTokens(Account account, TokenType tokenType);

    /**
     * Retrieves a verification token by its string value
     * @param token the token String to find
     * @return the verification token if found
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * Deletes a token after it has been used
     * @param token the token to be deleted
     */
    void deleteToken(VerificationToken token);
}
