package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.requests.delivery.ConfirmDeliveryRequest;
import com.courigistics.courigisticsbackend.dto.requests.delivery.CreateDeliveryRequest;
import com.courigistics.courigisticsbackend.dto.requests.delivery.DeliveryQuoteRequest;
import com.courigistics.courigisticsbackend.dto.responses.delivery.DeliveryCreationResponse;
import com.courigistics.courigisticsbackend.dto.responses.delivery.DeliveryResponse;
import com.courigistics.courigisticsbackend.dto.responses.delivery.TierOptionResponse;
import com.courigistics.courigisticsbackend.dto.responses.delivery.TrackingResponse;
import com.courigistics.courigisticsbackend.services.delivery.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/quote")
    public ResponseEntity<Map<String, Object>> getQuote(@Valid @RequestBody DeliveryQuoteRequest request) {
        List<TierOptionResponse> tiers = deliveryService.getQuote(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", tiers,
                "message", "Quote generated successfully"
        ));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<DeliveryCreationResponse> createDelivery(
            Authentication auth,
            @Valid @RequestBody CreateDeliveryRequest request
    ) {
        return ResponseEntity.ok(deliveryService.createDelivery(auth, request));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<Void> confirmDelivery(
            Authentication auth,
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmDeliveryRequest request
    ) {
        deliveryService.confirmDelivery(auth, id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<Page<DeliveryResponse>> getMyDeliveries(
            Authentication auth,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(deliveryService.getCustomerDeliveries(auth, pageable));
    }

    @GetMapping("/{number}/track")
    public ResponseEntity<DeliveryResponse> trackDelivery(@PathVariable String number) {
        return ResponseEntity.ok(deliveryService.trackDelivery(number));
    }

    @GetMapping("/{id}/tracking-info")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    public ResponseEntity<TrackingResponse> getTrackingInfo(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.getTrackingInfo(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<Void> cancelDelivery(Authentication auth, @PathVariable UUID id) {
        deliveryService.cancelDelivery(auth, id);
        return ResponseEntity.noContent().build();
    }
}
