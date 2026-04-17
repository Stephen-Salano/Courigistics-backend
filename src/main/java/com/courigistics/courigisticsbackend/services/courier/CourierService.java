package com.courigistics.courigisticsbackend.services.courier;

import com.courigistics.courigisticsbackend.dto.responses.courier.CourierDashboardResponse;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;
import com.courigistics.courigisticsbackend.dto.responses.delivery.DeliveryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CourierService {

    /**
     * Retrieves the full profile of a courier, including vehicle details if applicable.
     */
    CourierProfileResponse getCourierProfile(UUID accountId);

    /**
     * Gets deliveries currently assigned to the courier.
     */
    Page<DeliveryResponse> getCourierAssignments(UUID accountId, Pageable pageable);

    /**
     * Gets daily statistics for the courier dashboard.
     */
    CourierDashboardResponse getCourierDashboardStats(UUID accountId);

    /**
     * Updates the current location and online status of a courier.
     */
    void updateCourierLocation(UUID accountId, Double lat, Double lon);
}
