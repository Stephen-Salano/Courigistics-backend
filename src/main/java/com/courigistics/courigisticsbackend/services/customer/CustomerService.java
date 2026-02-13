package com.courigistics.courigisticsbackend.services.customer;

import com.courigistics.courigisticsbackend.dto.responses.customer.CustomerProfileResponse;

import java.util.UUID;

public interface CustomerService {
    /**
     * Retrieves the full profile of a customer, including their addresses.
     *
     * @param accountId The UUID of the customer's account.
     * @return CustomerProfileResponse containing personal details and addresses.
     */
    CustomerProfileResponse getCustomerProfile(UUID accountId);
}
