package com.courigistics.courigisticsbackend.services.auth;

import com.courigistics.courigisticsbackend.config.security.JwtService;
import com.courigistics.courigisticsbackend.dto.requests.AddressDTO;
import com.courigistics.courigisticsbackend.dto.requests.auth.AuthRequest;
import com.courigistics.courigisticsbackend.dto.requests.auth.RegisterRequest;
import com.courigistics.courigisticsbackend.dto.requests.auth.ResetPasswordRequest;
import com.courigistics.courigisticsbackend.dto.responses.AuthResponse;
import com.courigistics.courigisticsbackend.entities.*;
import com.courigistics.courigisticsbackend.entities.enums.AccountType;
import com.courigistics.courigisticsbackend.entities.enums.TokenType;
import com.courigistics.courigisticsbackend.repositories.AccountRepository;
import com.courigistics.courigisticsbackend.repositories.CustomerRepository;
import com.courigistics.courigisticsbackend.repositories.RefreshTokenRepository;
import com.courigistics.courigisticsbackend.services.verification_token.VerificationTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service("customerAuthService")
public class CustomerAuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final HttpServletRequest request;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;
    private final CustomerRepository customerRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    // TODO: add fingerprinting, Parsers(os family etc) , CacheManager(redis)


    @Override
    @Transactional
    public Account registerAccount(RegisterRequest request) {
        try{
            log.debug("Validating registration data (username/email) for: {}", request.username());
            validateRegistrationData(request);
            log.debug("Validation passed: username/email are free.");

            // 1. Create all related entities in memory first
            Account account = createCustomerAccount(request);
            Customer customer = createCustomer(request, account);

            // 2. Establish bidirectional relationships
            account.setCustomer(customer);

            // Conditionally create and link address:
            if (request.addressDTO() != null){
                log.debug("AddressDTO provided, creating and linking address.");
                Address address = createAddressEntity(request.addressDTO(), account);
                account.setAddresses(List.of(address));
            }else {
                log.debug("No AddressDTO provided, skipping address creation");
            }

            // 3. Save the parent entity. Cascade will handle the rest.
            log.debug("Attempting to save Account and cascade to Customer and Address.");
            Account savedAccount = accountRepository.save(account);
            log.debug("Saved Account with ID: {}", savedAccount.getId());

            // Generating the verification token
            log.debug("Creating Verification token for accountId={}", savedAccount.getId());
            VerificationToken verificationToken = verificationTokenService.createToken(
                    savedAccount, TokenType.EMAIL_VERIFICATION
            );
            log.debug("Created verificationToken: token={} expiresAt={}",
                    verificationToken.getToken(),
                    verificationToken.getExpiryDate()
            );

            // TODO: Add event publisher that will handle sending the email for verification
            log.info("Registration completed for user: {}", savedAccount.getUsername());
            return savedAccount;
        } catch (Exception e){
            log.error("Registration failed inside registerCustomer(): {}", e.getMessage());
            throw e;
        }
    }

    private Customer createCustomer(RegisterRequest request, Account account) {
        return Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .account(account)
                .build();
    }


    private Account createCustomerAccount(RegisterRequest request) {
         return Account.builder()
                .username(request.username())
                .email(request.email())
                .phone(request.phoneNumber()) // Added missing phone number
                .password(passwordEncoder.encode(request.password()))
                .accountType(AccountType.CUSTOMER)
                .enabled(false) // Should be false until email is verified
                .emailVerified(false)
                .accountNonLocked(true)
                .build();
    }

    private Address createAddressEntity(
            @NotBlank(message = "Address must be entered") AddressDTO addressDTO,
            Account account
    ) {

        Address addressEntity = new Address();
        addressEntity.setAccount(account);

        if (addressDTO.label().isBlank()){
            addressEntity.setLabel("Home address");
        }else {
            addressEntity.setLabel(addressDTO.label());
        }
        addressEntity.setAddressLine1(addressDTO.addressLine1());
        addressEntity.setAddressLine2(addressDTO.addressLine2());
        addressEntity.setCity(addressDTO.city());
        addressEntity.setPostalCode(addressDTO.postalCode());
        addressEntity.setCountry(addressDTO.country());

        // TODO: We will get the lats and longs from where the user's device is or when the user sets a Location for delivery
        return addressEntity;
    }


    private void validateRegistrationData(RegisterRequest request) {
        // We check if the username already exists
        if(accountRepository.existsByUsername(request.username())){
            throw new IllegalArgumentException("Username already exists");
        }

        // check if email already exists
        if (accountRepository.existsByEmail(request.email())){
            throw new IllegalArgumentException("Email already in use");
        }
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        // Validate the token
        Optional<VerificationToken> verificationTokenOpt = verificationTokenService.validateToken(token, TokenType.EMAIL_VERIFICATION);

        // if empty return false
        if (verificationTokenOpt.isEmpty()){
            log.info("Invalid or expired verification token:{}", token);
            return false;
        }

        // If token is already used
        if (verificationTokenOpt.get().getUsed() == true){
            log.info("Account Verification Token is already used:{}", token);
            return false;
            // TODO: Create custom exceptions for this problem
        }

        // Get the optional token
        VerificationToken verificationToken = verificationTokenOpt.get();
        // Get the account behind the optional token
        Account account = verificationToken.getAccount();

        // Enable the account
        account.setEnabled(true);
        accountRepository.save(account);

        // Mark the token as used
        verificationToken.setUsed(true);
        verificationTokenService.deleteToken(verificationToken);

        //TODO: Event publisher to begin deletion and send email template

        log.info("Email verified successfully for user: {}", account.getCustomer());
        return true;
    }

    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt for user: {}", request.usernameOrEmail());

        try{
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.usernameOrEmail(),
                            request.password()
                    )
            );

            // Get the authenticated account principal
            Account principalAccount = (Account) authentication.getPrincipal();

            // Re-fetch the account to ensure it's a managed entity in the current transaction
            // This prevents issues with detached entities caused by other transactional listeners
            Account account = accountRepository.findByUsername(principalAccount.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found in database post-authentication"));

            // Check if account is enabled
            if (!account.isEnabled()){
                throw new IllegalArgumentException("Account not verified. Please check your email for the verification link");
            }

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(account);
            String refreshToken = jwtService.generateRefreshToken(account);

            // Save the refresh token to the database
            saveRefreshToken(account, refreshToken);

            // TODO: Add device fingerprint for unknown login alerts to users
            log.info("Login successful for user:{}", account.getUsername());
            return AuthResponse.of(
                    accessToken, refreshToken,jwtService.getAccessTokenExpiration() / 1000, // converted into seconds
                    account.getUsername(), account.getEmail(), account.getAccountType().name()
                    );
        } catch (AuthenticationException e){
            log.warn("Login failed for user: {} - {}", request.usernameOrEmail(), e.getMessage());
            throw new IllegalArgumentException("Invalid username / email or password");
        }
    }

    private void saveRefreshToken(Account account, String refreshToken) {
        // Invalidate all previous refreshToken for this user for enhanced security
        refreshTokenRepository.invalidateAllByAccount(account);

        // create a new RefreshToken
        RefreshToken newRefreshToken = RefreshToken.builder()
                .account(account)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                .invalidated(false)
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(newRefreshToken);
    }

    @Override
    public void logout(Authentication authentication) {

    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        return null;
    }

    @Override
    public void requestPasswordReset(String email) {

    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

    }
}
