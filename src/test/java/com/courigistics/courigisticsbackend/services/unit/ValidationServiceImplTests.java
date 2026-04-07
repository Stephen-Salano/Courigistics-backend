package com.courigistics.courigisticsbackend.services.unit;

import com.courigistics.courigisticsbackend.repositories.AccountRepository;
import com.courigistics.courigisticsbackend.repositories.CourierRepository;
import com.courigistics.courigisticsbackend.repositories.VehicleRepository;
import com.courigistics.courigisticsbackend.services.validation.ValidationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private ValidationServiceImpl validationService;

    @Test
    @DisplayName("Should return available true when email does not exist")
    void checkEmail_Available() {
        when(accountRepository.existsByEmail("test@example.com")).thenReturn(false);
        assertTrue(validationService.isAvailable("email", "test@example.com"));
    }

    @Test
    @DisplayName("Should return available false when email exists")
    void checkEmail_Taken() {
        when(accountRepository.existsByEmail("taken@example.com")).thenReturn(true);
        assertFalse(validationService.isAvailable("email", "taken@example.com"));
    }

    @Test
    @DisplayName("Should return available true when username does not exist")
    void checkUsername_Available() {
        when(accountRepository.existsByUsername("newuser")).thenReturn(false);
        assertTrue(validationService.isAvailable("username", "newuser"));
    }

    @Test
    @DisplayName("Should return available true when phone does not exist (normalized)")
    void checkPhone_Available() {
        when(accountRepository.existsByPhone("254712345678")).thenReturn(false);
        assertTrue(validationService.isAvailable("phone", "0712345678"));
    }

    @Test
    @DisplayName("Should return available true when national ID does not exist")
    void checkNationalId_Available() {
        when(courierRepository.existsByNationalId("12345678")).thenReturn(false);
        assertTrue(validationService.isAvailable("nationalId", "12345678"));
    }

    @Test
    @DisplayName("Should throw exception for unknown validation type")
    void checkUnknownType_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> validationService.isAvailable("invalidType", "value"));
    }

    @Test
    @DisplayName("Should return false for null or blank value")
    void checkNullValue_ReturnsFalse() {
        assertFalse(validationService.isAvailable("email", null));
        assertFalse(validationService.isAvailable("email", "  "));
    }
}
