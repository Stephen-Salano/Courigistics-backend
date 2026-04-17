package com.courigistics.courigisticsbackend.services.admin;

import com.courigistics.courigisticsbackend.dto.responses.admin.AdminDashboardResponse;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    /**
     * Retrieves aggregated statistics for the admin dashboard.
     */
    AdminDashboardResponse getDashboardStats();

    /**
     * Gets a list of couriers awaiting approval.
     */
    List<CourierProfileResponse> getPendingCouriers();

    /**
     * Approves a courier registration.
     */
    void approveCourier(UUID courierId);
}
