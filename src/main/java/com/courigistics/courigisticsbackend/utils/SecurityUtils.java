package com.courigistics.courigisticsbackend.utils;

import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.exceptions.AccessDeniedException;
import org.springframework.security.core.Authentication;


public class SecurityUtils {

    private SecurityUtils() {
    }

    public static Account getAuthenticatedAccount(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()){
            throw new AccessDeniedException("No authenticated user found");
        }

        if (!(authentication.getPrincipal() instanceof Account account)){
            throw new AccessDeniedException("Unexpected principal type");
        }

        return account;
    }
}
