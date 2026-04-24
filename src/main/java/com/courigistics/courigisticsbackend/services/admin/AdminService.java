package com.courigistics.courigisticsbackend.services.admin;

import com.courigistics.courigisticsbackend.dto.requests.admin.AdminCreateRequest;
import com.courigistics.courigisticsbackend.dto.requests.admin.AdminUpdateRequest;
import com.courigistics.courigisticsbackend.dto.requests.admin.CourierAdminUpdateRequest;
import com.courigistics.courigisticsbackend.dto.requests.admin.CustomerAdminUpdateRequest;
import com.courigistics.courigisticsbackend.dto.requests.admin.DepotRequest;
import com.courigistics.courigisticsbackend.dto.responses.admin.AdminDashboardResponse;
import com.courigistics.courigisticsbackend.dto.responses.admin.AdminResponse;
import com.courigistics.courigisticsbackend.dto.responses.admin.CustomerResponse;
import com.courigistics.courigisticsbackend.dto.responses.admin.DepotResponse;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;
import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    /**
     * Retrieves aggregated statistics for the admin dashboard.
     */
    AdminDashboardResponse getDashboardStats();

    // --- Courier Management ---

    /**
     * Gets a list of couriers awaiting approval.
     */
    List<CourierProfileResponse> getPendingCouriers();

    /**
     * Approves a courier registration.
     */
    void approveCourier(UUID courierId);

    /**
     * Retrieves a paginated list of all couriers.
     */
    Page<CourierProfileResponse> getAllCouriers(Pageable pageable);

    /**
     * Updates only the status of a courier (e.g., ACTIVE, BLOCKED).
     */
    void updateCourierStatus(UUID courierId, CourierStatus status);

    /**
     * Updates multiple courier profile details.
     */
    void updateCourier(UUID courierId, CourierAdminUpdateRequest request);

    /**
     * Permanently removes a courier from the system.
     */
    void deleteCourier(UUID courierId);

    // --- Admin Management ---

    AdminResponse createAdmin(AdminCreateRequest request);
    List<AdminResponse> getAllAdmins();
    AdminResponse getAdminById(UUID adminId);
    AdminResponse updateAdmin(UUID adminId, AdminUpdateRequest request);
    void deleteAdmin(UUID adminId);

    // --- Depot Management ---

    DepotResponse createDepot(DepotRequest request);
    List<DepotResponse> getAllDepots();
    DepotResponse getDepotById(UUID depotId);
    DepotResponse updateDepot(UUID depotId, DepotRequest request);
    void deleteDepot(UUID depotId);

    // --- Customer Management ---

    Page<CustomerResponse> getAllCustomers(Pageable pageable);
    CustomerResponse getCustomerById(UUID customerId);
    CustomerResponse updateCustomer(UUID customerId, CustomerAdminUpdateRequest request);
    void deleteCustomer(UUID customerId);
}
