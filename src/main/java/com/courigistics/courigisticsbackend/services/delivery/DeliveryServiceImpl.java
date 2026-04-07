package com.courigistics.courigisticsbackend.services.delivery;

import com.courigistics.courigisticsbackend.dto.requests.delivery.ConfirmDeliveryRequest;
import com.courigistics.courigisticsbackend.dto.requests.delivery.CreateDeliveryRequest;
import com.courigistics.courigisticsbackend.dto.requests.delivery.DeliveryQuoteRequest;
import com.courigistics.courigisticsbackend.dto.responses.delivery.DeliveryCreationResponse;
import com.courigistics.courigisticsbackend.dto.responses.delivery.TierOptionResponse;
import com.courigistics.courigisticsbackend.entities.*;
import com.courigistics.courigisticsbackend.entities.enums.DeliveryStatus;
import com.courigistics.courigisticsbackend.entities.enums.RouteType;
import com.courigistics.courigisticsbackend.exceptions.ResourceNotFoundException;
import com.courigistics.courigisticsbackend.repositories.DeliveryRepository;
import com.courigistics.courigisticsbackend.repositories.PackageRepository;
import com.courigistics.courigisticsbackend.services.courier.CourierAssignmentService;
import com.courigistics.courigisticsbackend.services.depot.DepotService;
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
        log.info("Generating delivery quote for distance: {}km", request.googleMapsDistanceKm());

        // We use the Nearest Depot's city for finding candidate couriers
        Depot nearestDepot = depotService.findNearestDepotFor(request.pickupLat(), request.pickupLon());

        return courierAssignmentService.getAvailableTiers(
                request.pickupLat(),
                request.pickupLon(),
                request.packageCategory(),
                request.isFragile() != null && request.isFragile(),
                request.googleMapsDistanceKm(),
                nearestDepot.getCity()
        );
    }

    @Override
    public DeliveryCreationResponse createDelivery(Authentication auth, CreateDeliveryRequest request) {
        Account sender = SecurityUtils.getAuthenticatedAccount(auth);
        log.info("Creating delivery for customer: {}", sender.getEmail());

        // 1. Determine Depot Coverage & Routing
        Depot originDepot = depotService.findNearestDepotFor(request.pickupLat(), request.pickupLon());
        Depot destinationDepot = depotService.findNearestDepotFor(request.dropOffLat(), request.dropOffLon());

        RouteType routeType = depotService.requiresIntercityTransfer(originDepot, destinationDepot)
                ? RouteType.INTERCITY
                : RouteType.LOCAL;

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
                .originDepot(originDepot)
                .destinationDepot(destinationDepot)
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
                originDepot.getCity()
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
        // Implementation for courier/admin updates
    }

    @Override
    public void cancelDelivery(Authentication auth, UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        delivery.setDeliveryStatus(DeliveryStatus.CANCELLED);
        deliveryRepository.save(delivery);
    }

    private DeliveryResponse mapToResponse(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getDeliveryNumber(),
                delivery.getPackages().getTrackingNumber(),
                delivery.getDeliveryStatus(),
                delivery.getEstimatedPrice(),
                delivery.getRouteType(),
                delivery.getPickupAddress().getAddressLine1(),
                delivery.getDropoffAddress().getAddressLine1(),
                delivery.getCreatedAt(),
                delivery.getCourier() != null ? delivery.getCourier().getFirstName() : "Searching..."
        );
    }

    private String generateTrackingNumber(String prefix) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "COU-" + prefix + "-" + date + "-" + random;
    }
}
