package com.courigistics.courigisticsbackend.services.unit;


import com.courigistics.courigisticsbackend.config.security.JwtService;
import com.courigistics.courigisticsbackend.dto.requests.common.AddressDTO;
import com.courigistics.courigisticsbackend.dto.requests.auth.LoginRequest;
import com.courigistics.courigisticsbackend.dto.requests.customer.CustomerRegisterRequest;
import com.courigistics.courigisticsbackend.dto.responses.auth.AuthResponse;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.Customer;
import com.courigistics.courigisticsbackend.entities.RefreshToken;
import com.courigistics.courigisticsbackend.entities.VerificationToken;
import com.courigistics.courigisticsbackend.entities.enums.AccountType;
import com.courigistics.courigisticsbackend.entities.enums.TokenType;
import com.courigistics.courigisticsbackend.repositories.AccountRepository;
import com.courigistics.courigisticsbackend.repositories.RefreshTokenRepository;
import com.courigistics.courigisticsbackend.services.auth.CustomerAuthServiceImpl;
import com.courigistics.courigisticsbackend.services.verification_token.VerificationTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test examples:
 * 1. Happy Path (register a customer with valid details should create account)
 * 2. Input validation scenarios
 */
@ExtendWith(MockitoExtension.class)
public class CustomerAuthServiceImplTests {

    @InjectMocks
    CustomerAuthServiceImpl authService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication mockAuthentication;

    /**
     * Helper method to create an RegistrationRequest with a valid Adrress
     * @return the registerRequest for account creation with valid Address
     */
    private CustomerRegisterRequest createValidRegisterRequestWithAddress(){
        AddressDTO validAddress = new AddressDTO(
                "home",
                "123 Avery Lane",
                "1234 Avery Lane 2",
                "Nairobi",
                "36463-0100",
                "Kenya",
                true
        );

        return new CustomerRegisterRequest(
                "testuser@gmail.com",
                "testusr",
                "testUser254$",
                "test",
                "user",
                "1234567890",
                "01234567890",
                validAddress
        );
    }

    private CustomerRegisterRequest createValidregisterRequestWithoutAddress(){
        return new CustomerRegisterRequest(
                "testuser@gmail.com",
                "testusr",
                "testUser254$",
                "test",
                "user",
                "1234567890",
                "01234567890",
                null
        );
    }

    private LoginRequest createValidAuthRequest(){
        return new LoginRequest(
                "testusr",
                "testUser254$"
        );
    }



    @Test
    @DisplayName("Happy path registration")
    public void registerCustomer_withValidAndUniqueData_shouldCreateAccountAccountSuccessfully(){

        CustomerRegisterRequest request = createValidRegisterRequestWithAddress();

        // --- ARRANGE ---
        // 1. Mock repository checks for username/email
        Mockito.when(accountRepository.existsByUsername(request.username())).thenReturn(false);
        Mockito.when(accountRepository.existsByEmail(request.email())).thenReturn(false);

        // 2. Mock password encoding
        String hashedPassword = "hashedPassword123";
        Mockito.when(passwordEncoder.encode(request.password())).thenReturn(hashedPassword);

        // 3. Mock the account saving to return the account that was passed to it
        // `thenAnswer()`calculates he return value dynamically at the moment the method is called
        // invocation.getArgument(0) -> inside he answer block, invocation represents the specific method call being made
        //  `.getArgument(0)` retrieves the first argument passed to the save method
        Mockito.when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 4. Mock the token creation
        Mockito.when(verificationTokenService.createToken(any(Account.class), Mockito.eq(TokenType.EMAIL_VERIFICATION)))
                .thenReturn(new VerificationToken()); // Return a dummy token

        // --- ACT ---
        Account resultAccount = authService.registerAccount(request);

        // --- ASSERT ---
        // Verify the returned account is correctly configured
        assertNotNull(resultAccount);
        assertEquals("testusr", resultAccount.getUsername());
        assertEquals(hashedPassword, resultAccount.getPassword());
        assertFalse(resultAccount.getEnabled(), "Account should be disabled until email verification");
        assertFalse(resultAccount.getEmailVerified(), "Email should not be verified yet");

        // Verify that the createToken method was called exactly once with the correct arguments
        verify(verificationTokenService).createToken(resultAccount, TokenType.EMAIL_VERIFICATION);
    }

    @Test
    public void registerCustomer_withNoAddressInformation_shouldCreateAccountAccount(){
        CustomerRegisterRequest request = createValidregisterRequestWithoutAddress();
        String hashedPassword = "hashedPassword123";
        
        // Mocks
        Mockito.when(accountRepository.existsByUsername(request.username())).thenReturn(false);
        Mockito.when(accountRepository.existsByEmail(request.email())).thenReturn(false);
        Mockito.when(passwordEncoder.encode(request.password())).thenReturn(hashedPassword);
        Mockito.when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(verificationTokenService.createToken(any(Account.class), Mockito.eq(TokenType.EMAIL_VERIFICATION)))
                .thenReturn(new VerificationToken()); // Return a dummy token

        Account resultAccount = authService.registerAccount(request);

        // Assertions
        assertNotNull(resultAccount);
        assertEquals("testusr", resultAccount.getUsername());
        assertFalse(resultAccount.getEnabled(), "Account should be disabled until email verification");
        assertFalse(resultAccount.getEmailVerified(), "Email should not be verified yet");
        assertNull(resultAccount.getAddresses());

        verify(accountRepository).save(any()); // save was never called
        verify(verificationTokenService).createToken(resultAccount, TokenType.EMAIL_VERIFICATION); // token creation was never called

    }

    /**
     * The goal is to prevent duplicate
     */
    @Test
    public void registerAccount_withExistingUsername_shouldThrowException(){
        CustomerRegisterRequest request = createValidRegisterRequestWithAddress();

        Mockito.when(accountRepository.existsByUsername(request.username())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->{
            authService.registerAccount(request);
        });

        // verify that save was never called
        verify(accountRepository, never()).save(any());
    }

    @Test
    public void loginAccount_withExistingAccountDetails_shouldSucceed(){
        LoginRequest loginRequest = createValidAuthRequest();

        Customer sampleCustomer = new Customer().builder()
                .firstName("john")
                .lastName("doe")
                .profileImageUrl("/profile/")
                .build();

        // create the expected user account that will be "authenticated"
        Account authenticatedAccount = new Account().builder()
                .username("testusr")
                .email("testuser@gmail.com")
                .enabled(true)
                .accountType(AccountType.CUSTOMER)
                .emailVerified(true)
                .customer(sampleCustomer)
                .build();

        // Mock behavior
        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).
                thenReturn(mockAuthentication);
        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(authenticatedAccount);
        Mockito.when(accountRepository.findByUsername("testusr")).thenReturn(Optional.of(authenticatedAccount));
        Mockito.when(jwtService.generateAccessToken(authenticatedAccount)).thenReturn("dummy-access-token");
        Mockito.when(jwtService.generateRefreshToken(authenticatedAccount)).thenReturn("dummy-refresh-token");
        Mockito.when(jwtService.getAccessTokenExpiration()).thenReturn(3600000L);

        // ACT
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("dummy-access-token", response.accessToken());
        assertEquals("dummy-refresh-token", response.refreshToken());
        assertEquals("testusr", response.username());
        assertEquals(3600, response.expiresIn());

        // Verify that key methods were called on the mocks
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenRepository).invalidateAllByAccount(authenticatedAccount);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(jwtService).generateAccessToken(authenticatedAccount);
    }

    /*
    TODO: Implement tests for:
        - throw IllegalArgumentException for invalid credentials
        - throw IllegalArgumentException for disabled account
     */

}
