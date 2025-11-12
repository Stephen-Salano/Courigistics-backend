package com.courigistics.courigisticsbackend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "external_products")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class ExternalProducts {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", unique = true, nullable = false, updatable = true)
    private Packages packages;

    @Column(name = "source_platform")
    private String sourcePlatform;

    @Column(name = "productName")
    private String productName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "currency")
    private String currency;

    @Column(name = "expected_arrival_date")
    private LocalDateTime expectedDateArrival;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
