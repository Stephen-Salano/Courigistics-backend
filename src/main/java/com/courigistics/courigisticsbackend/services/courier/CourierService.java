package com.courigistics.courigisticsbackend.services.courier;

import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;

import java.util.UUID;

public interface CourierService {

    /**
     * Retrieves the full profile of a courier, including vehicle details if applicable.
     *
     * @param accountId The UUID of the courier's account.
     * @return CourierProfileResponse containing personal, employment, and vehicle details.
     */
    CourierProfileResponse getCourierProfile(UUID accountId);
}
