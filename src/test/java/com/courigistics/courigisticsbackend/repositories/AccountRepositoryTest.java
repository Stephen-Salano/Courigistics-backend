package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.Address;
import com.courigistics.courigisticsbackend.entities.Customer;
import com.courigistics.courigisticsbackend.entities.enums.AccountType;
import org.assertj.core.api.Assertions;
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

    @Test
    public void findByUsername_givenExitingUsername_thenReturnAccount(){

        // Arrange
        Address homeAddress = Address.builder()
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

        Customer salano = Customer.builder()
                .firstName("Stephen")
                .lastName("Salano")
                .profileImageUrl("/url/profile")
                .build();

        Account newAccount = Account.builder()
                .username("salano")
                .email("salano@gmail.com")
                .phone("0712345689")
                .password(passwordEncoder.encode("Microsoft2554$"))
                .accountType(AccountType.CUSTOMER)
                .enabled(true)
                .emailVerified(true)
                .build();

        // Act
        homeAddress.setAccount(newAccount);
        newAccount.setAddresses(List.of(homeAddress));
        salano.setAccount(newAccount);
        newAccount.setCustomer(salano);

        accountRepository.save(newAccount);
        Optional<Account> user = accountRepository.findByUsername(newAccount.getUsername());

        // Assert
        Assertions.assertThat(user).isPresent();
        Assertions.assertThat(user.get().getUsername()).isNotEmpty();
        Assertions.assertThat(user.get().getAccountType()).isEqualTo(AccountType.CUSTOMER);
    }

    @Test
    public void findByEmail_givenExistingEmail_thenReturnAccount(){
        // arrange
        // Arrange
        Address homeAddress = Address.builder()
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

        Customer salano = Customer.builder()
                .firstName("Stephen")
                .lastName("Salano")
                .profileImageUrl("/url/profile")
                .build();

        Account newAccount = Account.builder()
                .username("salano")
                .email("salano@gmail.com")
                .phone("0712345689")
                .password(passwordEncoder.encode("Microsoft2554$"))
                .accountType(AccountType.CUSTOMER)
                .enabled(true)
                .emailVerified(true)
                .build();

        // Act
        homeAddress.setAccount(newAccount);
        newAccount.setAddresses(List.of(homeAddress));
        salano.setAccount(newAccount);
        newAccount.setCustomer(salano);

        accountRepository.save(newAccount);

        // Act

        Optional<Account> user = accountRepository.findByEmail(newAccount.getEmail());
        // assert

        Assertions.assertThat(user).isPresent(); // is present
        Assertions.assertThat(user.get().getEmail()).isNotNull(); // is not null
        Assertions.assertThat(user.get().getEmail()).isEqualTo(newAccount.getEmail()); // is the right email
    }
}
