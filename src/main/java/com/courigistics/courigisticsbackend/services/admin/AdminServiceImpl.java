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
import com.courigistics.courigisticsbackend.dto.responses.admin.RevenueChartData;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;
import com.courigistics.courigisticsbackend.entities.*;
import com.courigistics.courigisticsbackend.entities.enums.AccountType;
import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import com.courigistics.courigisticsbackend.entities.enums.TokenType;
import com.courigistics.courigisticsbackend.exceptions.BadRequestException;
import com.courigistics.courigisticsbackend.exceptions.DuplicateResourceException;
import com.courigistics.courigisticsbackend.exceptions.ResourceNotFoundException;
import com.courigistics.courigisticsbackend.repositories.*;
import com.courigistics.courigisticsbackend.services.email.EmailService;
import com.courigistics.courigisticsbackend.services.verification_token.VerificationTokenService;
import com.courigistics.courigisticsbackend.utils.EmployeeIdGenerator;
import com.courigistics.courigisticsbackend.utils.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminServiceImpl implements AdminService {

    private final DeliveryRepository deliveryRepository;
    private final CourierRepository courierRepository;
    private final AccountRepository accountRepository;
    private final AdminRepository adminRepository;
    private final DepotRepository depotRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeIdGenerator employeeIdGenerator;
    private final PasswordEncoder passwordEncoder;
    private final VehicleRepository vehicleRepository;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        log.info("Fetching admin dashboard statistics");

        BigDecimal totalRevenue = deliveryRepository.getTotalRevenue();
        Double totalDistance = deliveryRepository.getTotalDistanceCovered();
        long activeCouriers = courierRepository.countByIsOnlineTrue();
        long pendingApprovals = courierRepository.countByPendingApprovalTrue();
        long ongoingDeliveries = deliveryRepository.countOngoingDeliveries();

        AdminDashboardResponse.SummaryDTO summary = AdminDashboardResponse.SummaryDTO.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalDistance(totalDistance != null ? totalDistance : 0.0)
                .activeCouriers(activeCouriers)
                .pendingApprovals(pendingApprovals)
                .ongoingDeliveries(ongoingDeliveries)
                .build();

        // Fetch 5 most recent deliveries
        List<Delivery> recentDeliveries = deliveryRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        List<AdminDashboardResponse.RecentDeliveryDTO> recentDTOs = recentDeliveries.stream()
                .map(this::mapToRecentDeliveryDTO)
                .toList();

        // Dummy data for revenue chart for now
        List<RevenueChartData> chartData = new ArrayList<>();
        chartData.add(new RevenueChartData("Mon", BigDecimal.valueOf(5000)));
        chartData.add(new RevenueChartData("Tue", BigDecimal.valueOf(7500)));
        chartData.add(new RevenueChartData("Wed", BigDecimal.valueOf(4200)));
        chartData.add(new RevenueChartData("Thu", BigDecimal.valueOf(8100)));
        chartData.add(new RevenueChartData("Fri", BigDecimal.valueOf(6900)));

        return AdminDashboardResponse.builder()
                .summary(summary)
                .recentDeliveries(recentDTOs)
                .revenueChart(chartData)
                .build();
    }

    // --- Courier Management ---

    @Override
    @Transactional(readOnly = true)
    public List<CourierProfileResponse> getPendingCouriers() {
        return courierRepository.findByPendingApprovalTrue().stream()
                .map(this::mapToCourierProfileResponse)
                .toList();
    }

    @Override
    public void approveCourier(UUID courierId) {
        log.info("Approving courier with ID: {}", courierId);
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));

        courier.setPendingApproval(false);
        courier.setStatus(CourierStatus.ACTIVE);
        courier.setApprovedAt(LocalDateTime.now());

        if (courier.getEmployeeId() == null) {
            courier.setEmployeeId(employeeIdGenerator.generateEmployeeId());
        }

        courierRepository.save(courier);

        // Send approval email
        VerificationToken setupToken = verificationTokenService.createToken(
                courier.getAccount(), TokenType.ACCOUNT_SETUP
        );

        if (courier.getEmploymentType() == EmploymentType.EMPLOYEE) {
            emailService.sendCourierEmployeeApprovalEmail(
                    courier.getAccount().getEmail(),
                    courier.getFirstName(),
                    courier.getEmployeeId(),
                    setupToken.getToken()
            );
        } else {
            emailService.sendCourierFreelancerApprovalEmail(
                    courier.getAccount().getEmail(),
                    courier.getFirstName(),
                    setupToken.getToken()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourierProfileResponse> getAllCouriers(Pageable pageable) {
        return courierRepository.findAll(pageable)
                .map(this::mapToCourierProfileResponse);
    }

    @Override
    public void updateCourierStatus(UUID courierId, CourierStatus status) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));
        courier.setStatus(status);
        courierRepository.save(courier);
    }

    @Override
    public void updateCourier(UUID courierId, CourierAdminUpdateRequest request) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));

        courier.setFirstName(request.firstName());
        courier.setLastName(request.lastName());
        courier.setStatus(request.status());
        courier.setEmploymentType(request.employmentType());
        courier.setAvailableForAssignment(request.availableForAssignment());

        if (request.depotId() != null) {
            Depot depot = depotRepository.findById(request.depotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Depot not found"));
            courier.setDepot(depot);
        }

        courierRepository.save(courier);
    }

    @Override
    public void deleteCourier(UUID courierId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));
        
        // Handle account deletion as well
        Account account = courier.getAccount();
        courierRepository.delete(courier);
        if (account != null) {
            accountRepository.delete(account);
        }
    }

    // --- Admin Management ---

    @Override
    public AdminResponse createAdmin(AdminCreateRequest request) {
        if (accountRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use");
        }
        if (accountRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already in use");
        }

        Account account = Account.builder()
                .username(request.username())
                .email(request.email())
                .phone(PhoneNumberUtils.normalizePhoneNumber(request.phone()))
                .password(passwordEncoder.encode(request.password()))
                .accountType(AccountType.ADMIN)
                .enabled(true)
                .emailVerified(true)
                .accountNonLocked(true)
                .build();

        Account savedAccount = accountRepository.save(account);

        Admin admin = Admin.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .employeeId(employeeIdGenerator.generateEmployeeId())
                .department(request.department())
                .account(savedAccount)
                .build();

        Admin savedAdmin = adminRepository.save(admin);
        return mapToAdminResponse(savedAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponse getAdminById(UUID adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        return mapToAdminResponse(admin);
    }

    @Override
    public AdminResponse updateAdmin(UUID adminId, AdminUpdateRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        admin.setFirstName(request.firstName());
        admin.setLastName(request.lastName());
        admin.setDepartment(request.department());

        Account account = admin.getAccount();
        account.setEmail(request.email());
        account.setPhone(PhoneNumberUtils.normalizePhoneNumber(request.phone()));
        accountRepository.save(account);

        return mapToAdminResponse(adminRepository.save(admin));
    }

    @Override
    public void deleteAdmin(UUID adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        
        Account account = admin.getAccount();
        adminRepository.delete(admin);
        if (account != null) {
            accountRepository.delete(account);
        }
    }

    // --- Depot Management ---

    @Override
    public DepotResponse createDepot(DepotRequest request) {
        if (depotRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Depot code already exists");
        }

        Depot depot = new Depot();
        mapRequestToDepot(request, depot);
        
        return mapToDepotResponse(depotRepository.save(depot));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepotResponse> getAllDepots() {
        return depotRepository.findAll().stream()
                .map(this::mapToDepotResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepotResponse getDepotById(UUID depotId) {
        Depot depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new ResourceNotFoundException("Depot not found"));
        return mapToDepotResponse(depot);
    }

    @Override
    public DepotResponse updateDepot(UUID depotId, DepotRequest request) {
        Depot depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new ResourceNotFoundException("Depot not found"));

        if (!depot.getCode().equals(request.code()) && depotRepository.existsByCode(request.code())) {
            throw new BadRequestException("New depot code already in use");
        }

        mapRequestToDepot(request, depot);
        return mapToDepotResponse(depotRepository.save(depot));
    }

    @Override
    public void deleteDepot(UUID depotId) {
        Depot depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new ResourceNotFoundException("Depot not found"));

        // Check for active couriers
        long courierCount = courierRepository.countByDepot_Id(depotId);
        if (courierCount > 0) {
            throw new BadRequestException("Cannot delete depot with " + courierCount + " assigned couriers");
        }

        // Check for active deliveries
        long deliveryCount = deliveryRepository.countByOriginDepot_IdOrDestinationDepot_Id(depotId, depotId);
        if (deliveryCount > 0) {
            throw new BadRequestException("Cannot delete depot with " + deliveryCount + " linked deliveries");
        }

        depotRepository.delete(depot);
    }

    // --- Customer Management ---

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(this::mapToCustomerResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return mapToCustomerResponse(customer);
    }

    @Override
    public CustomerResponse updateCustomer(UUID customerId, CustomerAdminUpdateRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());

        Account account = customer.getAccount();
        account.setEmail(request.email());
        account.setPhone(PhoneNumberUtils.normalizePhoneNumber(request.phone()));
        account.setEnabled(request.enabled());
        accountRepository.save(account);

        return mapToCustomerResponse(customerRepository.save(customer));
    }

    @Override
    public void deleteCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Account account = customer.getAccount();
        customerRepository.delete(customer);
        if (account != null) {
            accountRepository.delete(account);
        }
    }

    // --- Helper Methods ---

    private CustomerResponse mapToCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getAccount().getEmail(),
                customer.getAccount().getPhone(),
                customer.getAccount().getUsername(),
                customer.getAccount().isEnabled(),
                customer.getAccount().getEmailVerified(),
                customer.getAccount().getLastLogin(),
                customer.getAccount().getCreatedAt()
        );
    }

    private AdminDashboardResponse.RecentDeliveryDTO mapToRecentDeliveryDTO(Delivery delivery) {
        return AdminDashboardResponse.RecentDeliveryDTO.builder()
                .id(delivery.getId().toString())
                .deliveryNumber(delivery.getDeliveryNumber())
                .status(delivery.getDeliveryStatus().name())
                .customerName(delivery.getSender() != null ? delivery.getSender().getUsername() : "Unknown")
                .courierName(delivery.getCourier() != null ? delivery.getCourier().getFirstName() : "Unassigned")
                .amount(delivery.getEstimatedPrice())
                .build();
    }

    private CourierProfileResponse mapToCourierProfileResponse(Courier courier) {
        return CourierProfileResponse.builder()
                .id(courier.getId())
                .firstName(courier.getFirstName())
                .lastName(courier.getLastName())
                .email(courier.getAccount().getEmail())
                .phone(PhoneNumberUtils.formatForDisplay(courier.getAccount().getPhone()))
                .nationalId(courier.getNationalId())
                .employmentType(courier.getEmploymentType())
                .employeeId(courier.getEmployeeId())
                .status(courier.getStatus())
                .depotName(courier.getDepot() != null ? courier.getDepot().getName() : null)
                .depotCode(courier.getDepot() != null ? courier.getDepot().getCode() : null)
                .depotLat(courier.getDepot() != null ? courier.getDepot().getLatitude() : null)
                .depotLon(courier.getDepot() != null ? courier.getDepot().getLongitude() : null)
                .currentLat(courier.getCurrentLat())
                .currentLon(courier.getCurrentLon())
                .availableForAssignment(courier.getAvailableForAssignment())
                .vehicle(courier.getAccount().getCourier() != null ? mapToVehicleDTO(courier) : null)
                .build();
    }

    private CourierProfileResponse.VehicleDTO mapToVehicleDTO(Courier courier) {
        // Find vehicle for this courier
        return vehicleRepository.findByCourier_id(courier.getId())
                .map(v -> CourierProfileResponse.VehicleDTO.builder()
                        .vehicleType(v.getVehicleType().name())
                        .make(v.getMake())
                        .model(v.getModel())
                        .licensePlate(v.getLicencePlate())
                        .color(v.getVehicleColor())
                        .capacityKg(v.getVehicleCapacityKg())
                        .capacityM3(v.getVehicleCapacityM3())
                        .status(v.getStatus().name())
                        .build())
                .orElse(null);
    }

    private AdminResponse mapToAdminResponse(Admin admin) {
        return new AdminResponse(
                admin.getId(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getAccount().getEmail(),
                admin.getAccount().getPhone(),
                admin.getAccount().getUsername(),
                admin.getEmployeeId(),
                admin.getDepartment()
        );
    }

    private DepotResponse mapToDepotResponse(Depot depot) {
        return new DepotResponse(
                depot.getId(),
                depot.getName(),
                depot.getCode(),
                depot.getAddress(),
                depot.getCity(),
                depot.getCountry(),
                depot.getLatitude(),
                depot.getLongitude(),
                depot.getCoverageRadiusKm(),
                depot.getStatus(),
                depot.getDepotType()
        );
    }

    private void mapRequestToDepot(DepotRequest request, Depot depot) {
        depot.setName(request.name());
        depot.setCode(request.code());
        depot.setAddress(request.address());
        depot.setCity(request.city());
        depot.setCountry(request.country());
        depot.setLatitude(request.latitude());
        depot.setLongitude(request.longitude());
        depot.setCoverageRadiusKm(request.coverageRadiusKm());
        depot.setStatus(request.status());
        depot.setDepotType(request.depotType());
    }
}
