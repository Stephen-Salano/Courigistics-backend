package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByAccount(Account account);
    void deleteByAccount(Account account);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken rt SET rt.invalidated = true WHERE rt.account = :account")
    void invalidateAllByAccount(Account account);
}
