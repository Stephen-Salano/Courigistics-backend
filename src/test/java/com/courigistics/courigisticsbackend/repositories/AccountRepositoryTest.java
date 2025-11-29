package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.Address;
import com.courigistics.courigisticsbackend.entities.Customer;
import com.courigistics.courigisticsbackend.entities.enums.AccountType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class AccountRepositoryTest {


    @Autowired
    AccountRepository accountRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private Account testAccount;
    private Customer testCustomer;
    private Address testAddress;


    @BeforeEach
    public void setUpAccountEntity(){
        // Arrange
         testAddress = Address.builder()
                .label("home")
                .addressLine1("123 Avery Lane")
                .addressLine2("1234 Avery Lane 2")
                .city("Nairobi")
                .postalCode("36463-0100")
                .country("Kenya")
                .latitude(4.732849)
                .longitude(87.391527)
                .isDefault(true)
                .createdAt(LocalDateTime.now())
                .build();

        testCustomer = Customer.builder()
                .firstName("Stephen")
                .lastName("Salano")
                .profileImageUrl("/url/profile")
                .build();

        testAccount = Account.builder()
                .username("salano")
                .email("salano@gmail.com")
                .phone("0712345689")
                .password(passwordEncoder.encode("Microsoft2554$"))
                .accountType(AccountType.CUSTOMER)
                .enabled(true)
                .emailVerified(true)
                .build();

        // Act
       testAddress.setAccount(testAccount);
       testAccount.setAddresses(List.of(testAddress));
       testCustomer.setAccount(testAccount);
       testAccount.setCustomer(testCustomer);

        accountRepository.save(testAccount);

    }

    @Test
    @DisplayName("Should return an account given the existing username")
    public void findByUsername_givenExitingUsername_thenReturnAccount(){
        Optional<Account> user = accountRepository.findByUsername(testAccount.getUsername());

        // Assert
        Assertions.assertThat(user).isPresent();
        Assertions.assertThat(user.get().getUsername()).isNotEmpty();
        Assertions.assertThat(user.get().getAccountType()).isEqualTo(AccountType.CUSTOMER);
    }

    @Test
    @DisplayName("should return an account given an email")
    public void findByEmail_givenExistingEmail_thenReturnAccount(){
        Optional<Account> user = accountRepository.findByEmail(testAccount.getEmail());
        // assert

        Assertions.assertThat(user).isPresent(); // is present
        Assertions.assertThat(user.get().getEmail()).isNotNull(); // is not null
        Assertions.assertThat(user.get().getEmail()).isEqualTo(testAccount.getEmail()); // is the right email
    }

    @Test
    @DisplayName("Given existing username should return true")
    public void existsByUsername_givenExistingUsername_returnTrue(){
        boolean exists = accountRepository.existsByUsername(testAccount.getUsername());
        // Assert
        Assertions.assertThat(exists).isTrue();
    }

}
