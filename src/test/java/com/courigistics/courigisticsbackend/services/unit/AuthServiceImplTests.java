package com.courigistics.courigisticsbackend.services.unit;


import com.courigistics.courigisticsbackend.dto.requests.AddressDTO;
import com.courigistics.courigisticsbackend.dto.requests.auth.RegisterRequest;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.VerificationToken;
import com.courigistics.courigisticsbackend.entities.enums.TokenType;
import com.courigistics.courigisticsbackend.repositories.AccountRepository;
import com.courigistics.courigisticsbackend.services.auth.AuthServiceImpl;
import com.courigistics.courigisticsbackend.services.verification_token.VerificationTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Test examples:
 * 1. Happy Path (register a customer with valid details should create account)
 * 2. Input validation scenarios
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTests {

    @InjectMocks
    AuthServiceImpl authService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private VerificationTokenService verificationTokenService;

    /**
     * Helper method to create an RegistrationRequest with a valid Adrress
     * @return the registerRequest for account creation with valid Address
     */
    private RegisterRequest createValidRegisterRequestWithAddress(){
        AddressDTO validAddress = new AddressDTO(
                "home",
                "123 Avery Lane",
                "1234 Avery Lane 2",
                "Nairobi",
                "36463-0100",
                "Kenya",
                true
        );

        return new RegisterRequest(
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

    private RegisterRequest createValidregisterRequestWithoutAddress(){
        return new RegisterRequest(
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



    @Test
    @DisplayName("Happy path registration")
    public void registerCustomer_withValidAndUniqueData_shouldCreateCustomerAccountSuccessfully(){

        RegisterRequest request = createValidRegisterRequestWithAddress();

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
        Account resultAccount = authService.registerCustomer(request);

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
    public void registerCustomer_withNoAddressInformation_shouldCreateCustomerAccount(){
        RegisterRequest request = createValidregisterRequestWithoutAddress();
        String hashedPassword = "hashedPassword123";
        
        // Mocks
        Mockito.when(accountRepository.existsByUsername(request.username())).thenReturn(false);
        Mockito.when(accountRepository.existsByEmail(request.email())).thenReturn(false);
        Mockito.when(passwordEncoder.encode(request.password())).thenReturn(hashedPassword);
        Mockito.when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(verificationTokenService.createToken(any(Account.class), Mockito.eq(TokenType.EMAIL_VERIFICATION)))
                .thenReturn(new VerificationToken()); // Return a dummy token

        Account resultAccount = authService.registerCustomer(request);

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
    public void registerCustomer_withExistingUsername_shouldThrowException(){
        RegisterRequest request = createValidRegisterRequestWithAddress();

        Mockito.when(accountRepository.existsByUsername(request.username())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->{
            authService.registerCustomer(request);
        });

        // verify that save was never called
        verify(accountRepository, never()).save(any());
    }

}
