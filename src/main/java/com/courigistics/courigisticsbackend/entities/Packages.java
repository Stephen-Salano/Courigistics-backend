package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.PackageType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "package")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Packages {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_acc_id", unique = true, nullable = false)
    private Account senderAccount;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "package_type")
    @Enumerated(value = EnumType.STRING)
    private PackageType packageType;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "length_cm")
    private Double lengthCm;

    @Column(name = "width_cm")
    private Double widthCm;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "is_fragile")
    private Boolean isFragile;

    @Column(name = "is_insured")
    private Boolean isInsured;

    @Column(name = "declared_value")
    private BigDecimal declaredValue;

    @Column(name = "special_instructions")
    private String specialInstructions;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


}
