package com.courigistics.courigisticsbackend.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Runs on each request, looks for the access token (usually in Authorization: Bearer) asks JwtService to validate it
 * and if valid sets the SecurityContext so Spring knows who the user is
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final Environment environment;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 1: Skip filter for public endpoints if no token is present
        if (shouldNotFilter(request)){
            filterChain.doFilter(request, response);
            return;
        }

        // 2: Extract token and attempt authentication
        extractBearerToken(request).ifPresent(token -> {
            try{
                authenticateRequest(token, request, response);
            }catch (JwtException e){
                log.warn("Invalid JWT token recieved: {}, Details:{}", jwtService.getTokenMetadata(token), e.getMessage());
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            } catch (LockedException e){
                log.warn("Authentication attempt for a locked/disabled account: {}", e.getMessage());
                sendError(response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            } catch (Exception e){
                log.warn("An unexpected error occurred during JWT processing for user {}", jwtService.extractUsername(token), e);
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication processing error");
            }
        });

        // 3. continue the filter chain
        filterChain.doFilter(request, response);
    }

    private void sendError(@NonNull HttpServletResponse response, int httpStatus, String message) {
        if (response.isCommitted()){
            log.warn("Response already committed. Unable to send error {}: {}", httpStatus, message);
            return;
        }
    }

    private void authenticateRequest(String token, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
        // proceed only if there's no existing authentication
        if (SecurityContextHolder.getContext().getAuthentication() != null){
            log.warn("User is already authenticated. Skipping JWT processing");
            return;
        }

        String username = jwtService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (validateRequest(token, userDetails, request, response)){
            setAuthenticationContext(userDetails, request);
            log.info("Authenticated {} via JWT for request to {}", username, request.getRequestURI());
        }
    }

    /**
     * This method tells Spring that the user has been authenticated after JWT has been verified
     *
     * Think of it like logging the user into the SecurityContext without using a username and password
     * — because the JWT already proves who they are.
     * @param userDetails who the user is
     * @param request the sent request
     */
    private void setAuthenticationContext(UserDetails userDetails, @NonNull HttpServletRequest request) {


        /* creates an authentication object that Springboot understands
         * It holds:
         * - UserDetails -> who the user is
         * - null -> no password required (jwt already authenticated them)
         * - userDetails.getAuthorities -> the user roles or permissions
         *
         * authtoken obj basically says -> this user is valid and these are their roles
         */
        var authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        // Adding additional details from the request such as IP address, session id, user agent
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        /*
         * This stores the authenticated user in Spring Security’s global context for this request.
         * Once stored:
         *  - The controller can call SecurityContextHolder.getContext().getAuthentication()
         *  - @Preauthorize and other spring security rules work
         *  -
         */
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     * Groups all validation checks for an incoming authenticated request
     * @param token the jwt token
     * @param userDetails - the user
     * @param request - the passed request
     * @param response - the expected response
     * @return true if all checks pass, false if otherwise
     */
    private boolean validateRequest(String token, UserDetails userDetails, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
        // Standard token validation (expiry, audience)
        if (!jwtService.isTokenValid(token, userDetails)){
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return false;
        }
        validateAccountStatus(userDetails);

        Claims claims = jwtService.extractAllClaims(token);

        // Security validation (IP, fingerprint, Issuer)
        if (!validateIssuer(claims)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Security validation failed");
            return false;
        }
        return true;
    }

    private boolean validateIssuer(Claims claims) {
        if (!jwtService.getIssuer().equals(claims.getIssuer())){
            log.warn("Issuer mismatch: expected={}, actual={}", jwtService.getIssuer(), claims.getIssuer());
            return false;
        }
        return true;
    }

    /**
     * Centralized method to check the status of the user account based on the Userdetails contract
     * This ensures that even with a Valid JWT, a user who has been disabled, locked, or whose credentials have expired
     * cannot proceed
     *
     * @param userDetails The user details loaded from the database for the current request.
     * @throws LockedException if the account is locked, disabled, or expired
     */
    private void validateAccountStatus(UserDetails userDetails) {
        if (!userDetails.isAccountNonLocked()){
            throw new LockedException("Account is locked");
        }
        if (!userDetails.isEnabled()){
            throw new LockedException("Account is disabled");
        }
    }

    private Optional<String> extractBearerToken(@NonNull HttpServletRequest request) {
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader !=null && authHeader.startsWith(BEARER_PREFIX) && authHeader.length() > BEARER_PREFIX_LENGTH){
            return Optional.of(authHeader.substring(BEARER_PREFIX_LENGTH));
        }
        return Optional.empty();
    }

    // Ignore authentication for these paths
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/register")
                || path.startsWith("/api/v1/auth/verify")
                || path.startsWith("/api/v1/auth/health")
                || path.startsWith("/api/v1/auth/login")
                || path.startsWith("/h2-console")
                || path.startsWith("/actuator");
    }

    /*
    TODO: Methods to add for Security Context
    - validateIpAddress()
    - normalizeIp()
    - validateDeviceFingerprint()
    - setAuthenticationContext()
     */
}
