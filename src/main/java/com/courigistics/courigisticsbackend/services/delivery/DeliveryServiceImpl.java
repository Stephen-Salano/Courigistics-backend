package com.courigistics.courigisticsbackend.services.delivery;

import com.courigistics.courigisticsbackend.dto.requests.delivery.ConfirmDeliveryRequest;
import com.courigistics.courigisticsbackend.dto.requests.delivery.CreateDeliveryRequest;
import com.courigistics.courigisticsbackend.dto.requests.delivery.DeliveryMilestoneRequest;
import com.courigistics.courigisticsbackend.dto.requests.delivery.DeliveryQuoteRequest;
import com.courigistics.courigisticsbackend.dto.responses.delivery.DeliveryCreationResponse;
import com.courigistics.courigisticsbackend.dto.responses.delivery.DeliveryResponse;
import com.courigistics.courigisticsbackend.dto.responses.delivery.TierOptionResponse;
import com.courigistics.courigisticsbackend.dto.responses.delivery.TrackingResponse;
import com.courigistics.courigisticsbackend.entities.*;
import com.courigistics.courigisticsbackend.entities.enums.DeliveryMode;
import com.courigistics.courigisticsbackend.entities.enums.DeliveryStatus;
import com.courigistics.courigisticsbackend.entities.enums.PaymentStatus;
import com.courigistics.courigisticsbackend.entities.enums.RouteType;
import com.courigistics.courigisticsbackend.exceptions.ResourceNotFoundException;
import com.courigistics.courigisticsbackend.repositories.DeliveryRepository;
import com.courigistics.courigisticsbackend.repositories.PackageRepository;
import com.courigistics.courigisticsbackend.services.courier.CourierAssignmentService;
import com.courigistics.courigisticsbackend.services.depot.DepotService;
import com.courigistics.courigisticsbackend.utils.GeoUtils;
import com.courigistics.courigisticsbackend.utils.PriceCalculatorUtils;
import com.courigistics.courigisticsbackend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final PackageRepository packageRepository;
    private final DepotService depotService;
    private final CourierAssignmentService courierAssignmentService;

    @Override
    @Transactional(readOnly = true)
    public List<TierOptionResponse> getQuote(DeliveryQuoteRequest request) {
        log.info("[Quote] Generating quote for distance: {}km, pickup: ({}, {})", 
                request.googleMapsDistanceKm(), request.pickupLat(), request.pickupLon());

        Optional<Depot> nearestDepot = depotService.findOptionalNearestDepotFor(request.pickupLat(), request.pickupLon());
        String originCity = nearestDepot.map(Depot::getCity).orElseGet(() -> deriveCity(request.pickupLat(), request.pickupLon()));

        log.info("[Quote] Nearest depot: {}, origin city: {}", 
                nearestDepot.map(Depot::getCode).orElse("NONE"), originCity);

        List<TierOptionResponse> tiers = courierAssignmentService.getAvailableTiers(
                request.pickupLat(),
                request.pickupLon(),
                request.packageCategory(),
                request.isFragile() != null && request.isFragile(),
                request.googleMapsDistanceKm(),
                originCity
        );

        log.info("[Quote] Found {} available tiers for this request", tiers.size());
        return tiers;
    }

    @Override
    public DeliveryCreationResponse createDelivery(Authentication auth, CreateDeliveryRequest request) {
        Account sender = SecurityUtils.getAuthenticatedAccount(auth);
        log.info("Creating delivery for customer: {}", sender.getEmail());

        // 1. Determine Depot Coverage & Routing
        Optional<Depot> originDepot = depotService.findOptionalNearestDepotFor(request.pickupLat(), request.pickupLon());
        Optional<Depot> destinationDepot = depotService.findOptionalNearestDepotFor(request.dropOffLat(), request.dropOffLon());

        RouteType routeType = RouteType.LOCAL;
        DeliveryMode deliveryMode = DeliveryMode.DIRECT;

        if (originDepot.isPresent() && destinationDepot.isPresent()) {
            routeType = depotService.requiresIntercityTransfer(originDepot.get(), destinationDepot.get())
                    ? RouteType.INTERCITY
                    : RouteType.LOCAL;
            deliveryMode = (routeType == RouteType.INTERCITY) ? DeliveryMode.DEPOT_TO_DEPOT : DeliveryMode.DIRECT;
        }

        String originCity = originDepot.map(Depot::getCity).orElse(request.pickupCity());

        // 2. Create Package Entity
        Packages pkg = Packages.builder()
                .trackingNumber(generateTrackingNumber("PKG"))
                .packageCategory(request.packageCategory())
                .packageType(request.packageType())
                .description(request.description())
                .isFragile(request.isFragile() != null && request.isFragile())
                .isInsured(request.isInsured() != null && request.isInsured())
                .senderAccount(sender)
                .build();
        pkg = packageRepository.save(pkg);

        // 3. Create Address Entities
        Address pickupAddress = Address.builder()
                .addressLine1(request.pickupAddressLine())
                .city(request.pickupCity())
                .latitude(request.pickupLat())
                .longitude(request.pickupLon())
                .country("Kenya") // Default for now
                .account(sender)
                .build();

        Address dropoffAddress = Address.builder()
                .addressLine1(request.dropOffAddressLine())
                .city(request.dropOffCity())
                .latitude(request.dropOffLat())
                .longitude(request.dropOffLon())
                .country("Kenya") // Default for now
                .account(sender)
                .build();

        // 4. Create Delivery Entity
        Delivery delivery = Delivery.builder()
                .deliveryNumber(generateTrackingNumber("DEL"))
                .packages(pkg)
                .sender(sender)
                .recipientName(request.recipientName())
                .recipientPhone(request.recipientPhone())
                .originDepot(originDepot.orElse(null))
                .destinationDepot(destinationDepot.orElse(null))
                .deliveryMode(deliveryMode)
                .routeType(routeType)
                .pickupAddress(pickupAddress)
                .pickupLat(request.pickupLat())
                .pickupLon(request.pickupLon())
                .dropoffAddress(dropoffAddress)
                .dropOffLat(request.dropOffLat())
                .dropOffLon(request.dropOffLon())
                .estimatedDistanceKm(request.googleMapsDistanceKm())
                .routePolyline(request.routePolyline())
                .estimatedDurationMinutes(request.estimatedDurationMinutes())
                .deliveryStatus(DeliveryStatus.CREATED)
                .paymentMethod(request.paymentMethod())
                .requiresSignature(request.requiresSignature() != null && request.requiresSignature())
                .createdAt(LocalDateTime.now())
                .build();

        delivery = deliveryRepository.save(delivery);

        // 5. Calculate available tiers for this distance
        List<TierOptionResponse> tiers = courierAssignmentService.getAvailableTiers(
                request.pickupLat(),
                request.pickupLon(),
                request.packageCategory(),
                request.isFragile() != null && request.isFragile(),
                request.googleMapsDistanceKm(),
                originCity
        );

        return new DeliveryCreationResponse(
                delivery.getId(),
                delivery.getDeliveryNumber(),
                pkg.getTrackingNumber(),
                delivery.getEstimatedDistanceKm(),
                delivery.getRouteType(),
                tiers
        );
    }

    private String deriveCity(double lat, double lon) {
        // Fallback city derivation logic
        // Nairobi center: -1.286389, 36.817223
        double distToNairobi = GeoUtils.haversineDistance(lat, lon, -1.286389, 36.817223);
        if (distToNairobi < 100.0) return "Nairobi";
        
        // Mombasa center: -4.043477, 39.668206
        double distToMombasa = GeoUtils.haversineDistance(lat, lon, -4.043477, 39.668206);
        if (distToMombasa < 100.0) return "Mombasa";

        // Kisumu center: -0.091702, 34.767956
        double distToKisumu = GeoUtils.haversineDistance(lat, lon, -0.091702, 34.767956);
        if (distToKisumu < 100.0) return "Kisumu";

        return "Nairobi"; // Default fallback
    }

    @Override
    public void confirmDelivery(Authentication auth, UUID deliveryId, ConfirmDeliveryRequest request) {
        log.info("Confirming delivery: {} with tier: {}", deliveryId, request.selectedTier());
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        // Validate that the sender is the one confirming
        Account sender = SecurityUtils.getAuthenticatedAccount(auth);
        if (!delivery.getSender().getId().equals(sender.getId())) {
            throw new IllegalStateException("You are not authorized to confirm this delivery");
        }

        // Calculate final price based on selected tier
        BigDecimal finalPrice = PriceCalculatorUtils.calculatePrice(
                request.selectedTier(),
                delivery.getEstimatedDistanceKm(),
                delivery.getPackages().getIsFragile(),
                delivery.getPackages().getPackageCategory()
        );

        delivery.setEstimatedPrice(finalPrice);
        delivery.setDeliveryStatus(DeliveryStatus.PENDING_PAYMENT); // Or OFFERED depending on flow
        deliveryRepository.save(delivery);

        // For now, we simulate an immediate "Offer" to couriers
        // Phase 4 will handle the actual Dispatcher logic
        log.info("Delivery confirmed. Awaiting payment/dispatch.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeliveryResponse> getCustomerDeliveries(Authentication auth, Pageable pageable) {
        Account account = SecurityUtils.getAuthenticatedAccount(auth);
        return deliveryRepository.findBySender_Id(account.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse trackDelivery(String deliveryNumber) {
        Delivery delivery = deliveryRepository.findByDeliveryNumber(deliveryNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with number: " + deliveryNumber));
        return mapToResponse(delivery);
    }

    @Override
    public void updateDeliveryStatus(Authentication auth, UUID deliveryId, DeliveryStatus newStatus, String note) {
        Account account = SecurityUtils.getAuthenticatedAccount(auth);
        log.info("Updating delivery status: {} to {} by user: {}", deliveryId, newStatus, account.getEmail());

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        // Authorization: Only the assigned courier or an ADMIN can update status
        // TODO: Add ADMIN check once roles are fully implemented
        if (delivery.getCourier() == null || !delivery.getCourier().getAccount().getId().equals(account.getId())) {
            throw new IllegalStateException("You are not authorized to update this delivery");
        }

        delivery.setDeliveryStatus(newStatus);
        delivery.setUpdatedAt(LocalDateTime.now());

        if (newStatus == DeliveryStatus.PICKED_UP) {
            delivery.setActualPickupTime(LocalDateTime.now());
        } else if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setActualDeliveryTime(LocalDateTime.now());
            delivery.setPaymentStatus(PaymentStatus.PAID); // Simulation
        }

        deliveryRepository.save(delivery);
    }

    @Override
    public void cancelDelivery(Authentication auth, UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        delivery.setDeliveryStatus(DeliveryStatus.CANCELLED);
        deliveryRepository.save(delivery);
    }

    @Override
    public void updateDeliveryMilestone(Authentication auth, UUID deliveryId, DeliveryMilestoneRequest request) {
        log.info("Updating milestone for delivery: {} to {}", deliveryId, request.status());
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        delivery.setDeliveryStatus(request.status());
        delivery.setCurrentLat(request.currentLat());
        delivery.setCurrentLon(request.currentLon());

        // Increment actual distance
        Double currentDistance = delivery.getActualDistanceKm() != null ? delivery.getActualDistanceKm() : 0.0;
        delivery.setActualDistanceKm(currentDistance + request.distanceKm());

        // Set timestamps based on status
        if (request.status() == DeliveryStatus.ARRIVED_AT_PICKUP) {
            delivery.setArrivedAtPickupAt(LocalDateTime.now());
        } else if (request.status() == DeliveryStatus.PICKED_UP) {
            delivery.setActualPickupTime(LocalDateTime.now());
        } else if (request.status() == DeliveryStatus.ARRIVED_AT_DROPOFF) {
            delivery.setArrivedAtDropoffAt(LocalDateTime.now());
        } else if (request.status() == DeliveryStatus.DELIVERED) {
            delivery.setActualDeliveryTime(LocalDateTime.now());
            delivery.setPaymentStatus(PaymentStatus.PAID);
        }

        deliveryRepository.save(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingResponse getTrackingInfo(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        Courier courier = delivery.getCourier();
        String courierName = courier != null ? courier.getFirstName() + " " + courier.getLastName() : null;
        String courierPhone = (courier != null && courier.getAccount() != null) ? courier.getAccount().getPhone() : null;

        return new TrackingResponse(
                delivery.getDeliveryNumber(),
                delivery.getDeliveryStatus(),
                delivery.getCurrentLat(),
                delivery.getCurrentLon(),
                delivery.getActualDistanceKm(),
                delivery.getEstimatedDeliveryTime(),
                courierName,
                courierPhone,
                courier != null ? courier.getCurrentLat() : null,
                courier != null ? courier.getCurrentLon() : null
        );
    }

    private DeliveryResponse mapToResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .deliveryNumber(delivery.getDeliveryNumber())
                .trackingNumber(delivery.getPackages().getTrackingNumber())
                .status(delivery.getDeliveryStatus())
                .price(delivery.getEstimatedPrice())
                .routeType(delivery.getRouteType())
                .pickupAddress(delivery.getPickupAddress().getAddressLine1())
                .pickupLat(delivery.getPickupLat())
                .pickupLon(delivery.getPickupLon())
                .dropoffAddress(delivery.getDropoffAddress().getAddressLine1())
                .dropOffLat(delivery.getDropOffLat())
                .dropOffLon(delivery.getDropOffLon())
                .recipientName(delivery.getRecipientName())
                .recipientPhone(delivery.getRecipientPhone())
                .createdAt(delivery.getCreatedAt())
                .courierName(delivery.getCourier() != null ? delivery.getCourier().getFirstName() : "Searching...")
                .build();
    }

    private String generateTrackingNumber(String prefix) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "COU-" + prefix + "-" + date + "-" + random;
    }
}
