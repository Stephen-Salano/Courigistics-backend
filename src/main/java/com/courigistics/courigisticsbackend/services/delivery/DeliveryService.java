package com.courigistics.courigisticsbackend.services.delivery;

import com.courigistics.courigisticsbackend.dto.requests.delivery.ConfirmDeliveryRequest;
import com.courigistics.courigisticsbackend.dto.requests.delivery.CreateDeliveryRequest;
import com.courigistics.courigisticsbackend.dto.requests.delivery.DeliveryQuoteRequest;
import com.courigistics.courigisticsbackend.dto.responses.delivery.DeliveryCreationResponse;
import com.courigistics.courigisticsbackend.dto.responses.delivery.TierOptionResponse;
import com.courigistics.courigisticsbackend.entities.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing delivery lifecycle from quote to completion.
 */
public interface DeliveryService {

    /**
     * Gets available tiers and estimated prices for a potential delivery.
     */
    List<TierOptionResponse> getQuote(DeliveryQuoteRequest request);

    /**
     * Creates a new delivery record in CREATED status.
     * Determines routing (LOCAL vs INTERCITY) and calculates initial pricing.
     */
    DeliveryCreationResponse createDelivery(Authentication auth, CreateDeliveryRequest request);

    /**
     * Confirms a delivery and initiates the courier assignment (offer) flow.
     */
    void confirmDelivery(Authentication auth, UUID deliveryId, ConfirmDeliveryRequest request);

    /**
     * Gets deliveries for the currently authenticated customer.
     */
    Page<DeliveryResponse> getCustomerDeliveries(Authentication auth, Pageable pageable);

    /**
     * Public tracking of a delivery by its unique delivery number.
     */
    DeliveryResponse trackDelivery(String deliveryNumber);

    /**
     * Updates delivery status (used by couriers and internal processes).
     */
    void updateDeliveryStatus(Authentication auth, UUID deliveryId, DeliveryStatus newStatus, String note);

    /**
     * Cancels a delivery if it is in an allowed state (e.g., CREATED or OFFERED).
     */
    void cancelDelivery(Authentication auth, UUID deliveryId);
}
