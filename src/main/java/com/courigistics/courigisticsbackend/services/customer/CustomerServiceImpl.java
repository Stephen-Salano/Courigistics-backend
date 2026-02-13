package com.courigistics.courigisticsbackend.services.customer;

import com.courigistics.courigisticsbackend.dto.responses.customer.CustomerProfileResponse;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.Customer;
import com.courigistics.courigisticsbackend.exceptions.ResourceNotFoundException;
import com.courigistics.courigisticsbackend.repositories.AccountRepository;
import com.courigistics.courigisticsbackend.repositories.CustomerRepository;
import com.courigistics.courigisticsbackend.utils.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getCustomerProfile(UUID accountId) {
        log.info("Fetching customer profile for accountId: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        List<CustomerProfileResponse.AddressDTO> addressDTOs = account.getAddresses().stream()
                .map(addr -> new CustomerProfileResponse.AddressDTO(
                        addr.getId(),
                        addr.getLabel(),
                        addr.getAddressLine1(),
                        addr.getAddressLine2(),
                        addr.getCity(),
                        addr.getPostalCode(),
                        addr.getCountry(),
                        addr.isDefault()
                ))
                .collect(Collectors.toList());

        return new CustomerProfileResponse(
                customer.getFirstName(),
                customer.getLastName(),
                account.getEmail(),
                PhoneNumberUtils.formatForDisplay(account.getPhone()),
                customer.getProfileImageUrl(),
                addressDTOs
        );
    }
}
