package com.courigistics.courigisticsbackend.services.admin;

import com.courigistics.courigisticsbackend.dto.responses.admin.AdminDashboardResponse;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;
import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.Delivery;
import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import com.courigistics.courigisticsbackend.exceptions.ResourceNotFoundException;
import com.courigistics.courigisticsbackend.repositories.CourierRepository;
import com.courigistics.courigisticsbackend.repositories.DeliveryRepository;
import com.courigistics.courigisticsbackend.utils.EmployeeIdGenerator;
import com.courigistics.courigisticsbackend.utils.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final EmployeeIdGenerator employeeIdGenerator;

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
        List<com.courigistics.courigisticsbackend.dto.responses.admin.RevenueChartData> chartData = new ArrayList<>();
        chartData.add(new com.courigistics.courigisticsbackend.dto.responses.admin.RevenueChartData("Mon", BigDecimal.valueOf(5000)));
        chartData.add(new com.courigistics.courigisticsbackend.dto.responses.admin.RevenueChartData("Tue", BigDecimal.valueOf(7500)));
        chartData.add(new com.courigistics.courigisticsbackend.dto.responses.admin.RevenueChartData("Wed", BigDecimal.valueOf(4200)));
        chartData.add(new com.courigistics.courigisticsbackend.dto.responses.admin.RevenueChartData("Thu", BigDecimal.valueOf(8100)));
        chartData.add(new com.courigistics.courigisticsbackend.dto.responses.admin.RevenueChartData("Fri", BigDecimal.valueOf(6900)));

        return AdminDashboardResponse.builder()
                .summary(summary)
                .recentDeliveries(recentDTOs)
                .revenueChart(chartData)
                .build();
    }

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
    }

    private AdminDashboardResponse.RecentDeliveryDTO mapToRecentDeliveryDTO(Delivery delivery) {
        return AdminDashboardResponse.RecentDeliveryDTO.builder()
                .id(delivery.getId().toString())
                .deliveryNumber(delivery.getDeliveryNumber())
                .status(delivery.getDeliveryStatus().name())
                .customerName(delivery.getSender().getUsername())
                .courierName(delivery.getCourier() != null ? delivery.getCourier().getFirstName() : "Unassigned")
                .amount(delivery.getEstimatedPrice())
                .build();
    }

    private CourierProfileResponse mapToCourierProfileResponse(Courier courier) {
        return CourierProfileResponse.builder()
                .firstName(courier.getFirstName())
                .lastName(courier.getLastName())
                .email(courier.getAccount().getEmail())
                .phone(PhoneNumberUtils.formatForDisplay(courier.getAccount().getPhone()))
                .nationalId(courier.getNationalId())
                .employmentType(courier.getEmploymentType())
                .employeeId(courier.getEmployeeId())
                .status(courier.getStatus())
                .currentLat(courier.getCurrentLat())
                .currentLon(courier.getCurrentLon())
                .build();
    }
}
