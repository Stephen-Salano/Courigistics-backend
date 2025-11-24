package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.VerificationToken;
import com.courigistics.courigisticsbackend.entities.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByAccountAndTokenType(Account account, TokenType tokenType);
    void deleteByAccount(Account account);

}
