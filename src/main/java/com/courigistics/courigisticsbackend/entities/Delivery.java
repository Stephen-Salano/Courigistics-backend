package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "deliveries", indexes = {
        @Index(name = "idx_delivery_number", columnList = "delivery_number"),
        @Index(name = "idx_package_number", columnList = "tracking_number"),
        @Index(name = "idx_sender_acc_id", columnList = "")
})
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "delivery_number", unique = true, nullable = false)
    private String deliveryNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", unique = true, nullable = false)
    private Packages packages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_acc_id", unique = true, nullable = false)
    private Account sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_acc_id", unique = true)
    private Account recipient;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", unique = true, nullable = false, updatable = true)
    private Account courier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_depot_id", unique = true, nullable = false)
    private Depot originDepot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_depot_id", nullable = false, unique = true)
    private Depot destinationDepot;

    @Column(name = "delivery_mode")
    @Enumerated(value = EnumType.STRING)
    private DeliveryMode deliveryMode;

    @Column(name = "route_type")
    @Enumerated(value = EnumType.STRING)
    private RouteType routeType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "pickup_address_id", unique = true, updatable = true, nullable = false)
    private Address pickupAddress;

    @Column(name = "pickup_lat")
    private Double pickupLat;

    @Column(name = "pickup_lon")
    private Double pickupLon;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "dropoff_address_id", unique = true, updatable = true, nullable = false)
    private Address dropoffAddress;

    @Column(name = "dropoff_lat")
    private Double dropOffLat;

    @Column(name = "dropoff_lon")
    private Double dropOffLon;

    @Column(name = "estimated_distance_km")
    private Double estimatedDistanceKm;

    @Column(name = "actual_distance_km")
    private Double actualDistanceKm;

    @Column(name = "estimated_price")
    private BigDecimal estimatedPrice;

    @Column(name = "finalPrice")
    private BigDecimal finalPrice;

    @Column(name = "delivery_status")
    @Enumerated(value = EnumType.STRING)
    private DeliveryStatus deliveryStatus = DeliveryStatus.CREATED;

    @Column(name = "payment_method")
    @Enumerated(value = EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_status")
    @Enumerated(value = EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "scheduled_pickup_time")
    private List<LocalDateTime> scheduledPickupTime;

    @Column(name = "actual_pickup_time")
    private LocalDateTime actualPickupTime;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "available_for_pickup_at")
    private LocalDateTime availableForPickUpAt;

    @Column(name = "pickup_deadline")
    private LocalDateTime pickupDeadline;

    @Column(name = "requires_signature")
    private Boolean requiresSignature;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "proof_of_delivery_image_url")
    private String proofOfDeliveryUrl;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account cancelledByUser;

    @Column(name = "cancelledAt")
    private LocalDateTime cancelledAt;

    @Column(name = "refund_amount")
    private BigDecimal refundAmnt;

    @Column(name = "failed_reason")
    private String failedReason;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
