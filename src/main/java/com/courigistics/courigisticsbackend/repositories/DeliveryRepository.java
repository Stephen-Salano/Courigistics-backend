package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Delivery;
import com.courigistics.courigisticsbackend.entities.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    /**
     * Finds all deliveries sent by a customer
     * Used for customer delivery history page
     *
     * @param accountId customer's account ID
     * @param pageable  pagination parameters
     * @return paginated list of customer's deliveries
     */
    @Query("""
        SELECT d FROM Delivery d
        WHERE d.sender.id = :accountId
        ORDER BY d.createdAt DESC
        """)
    Page<Delivery> findBySender_Id(@Param("accountId") UUID accountId, Pageable pageable);

    /**
     * Finds all deliveries assigned to a courier
     * Used for courier dashboard and delivery list
     *
     * @param courierId courier's ID
     * @param pageable  pagination parameters
     * @return paginated list of courier's deliveries
     */
    @Query("""
        SELECT d FROM Delivery d
        WHERE d.courier.id = :courierId
        ORDER BY d.createdAt DESC
        """)
    Page<Delivery> findByCourier_Id(@Param("courierId") UUID courierId, Pageable pageable);

    /**
     * Finds delivery by unique delivery number
     * Used for customer tracking and admin lookup
     *
     * Example delivery number: COU-DEL-20260223-0001
     *
     * @param deliveryNumber the unique delivery identifier
     * @return delivery if found
     */
    Optional<Delivery> findByDeliveryNumber(String deliveryNumber);

    /**
     * Finds deliveries by status for a specific courier
     * Used for filtering courier's active/completed deliveries
     *
     * Example: Find all IN_TRANSIT deliveries for courier
     *
     * @param status    delivery status to filter by
     * @param courierId courier's ID
     * @return list of matching deliveries
     */
    @Query("""
        SELECT d FROM Delivery d
        WHERE d.deliveryStatus = :status
          AND d.courier.id = :courierId
        ORDER BY d.createdAt DESC
        """)
    List<Delivery> findByDeliveryStatusAndCourier_Id(
            @Param("status") DeliveryStatus status,
            @Param("courierId") UUID courierId
    );

    /**
     * Finds courier's active deliveries (not completed/cancelled)
     * Used to check if courier has ongoing deliveries before assigning new ones
     *
     * @param courierId courier's ID
     * @return list of active deliveries
     */
    @Query("""
        SELECT d FROM Delivery d
        WHERE d.courier.id = :courierId
          AND d.deliveryStatus IN ('PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'READY_FOR_PAYMENT')
        """)
    List<Delivery> findActiveByCourier_Id(@Param("courierId") UUID courierId);

    /**
     * Counts deliveries by status for a courier on a specific date
     * Used for daily statistics on courier dashboard
     *
     * @param courierId courier's ID
     * @param status    delivery status
     * @param date      date to filter by (based on createdAt)
     * @return count of deliveries
     */
    @Query("""
        SELECT COUNT(d) FROM Delivery d
        WHERE d.courier.id = :courierId
          AND d.deliveryStatus = :status
          AND DATE(d.createdAt) = :date
        """)
    long countByCourierAndStatusAndDate(
            @Param("courierId") UUID courierId,
            @Param("status") DeliveryStatus status,
            @Param("date") LocalDate date
    );

    /**
     * Finds all pending deliveries in a depot
     * Used for depot admin to see unassigned deliveries
     *
     * @param depotId depot ID
     * @return list of pending deliveries
     */
    @Query("""
        SELECT d FROM Delivery d
        WHERE d.originDepot.id = :depotId
          AND d.deliveryStatus = 'PENDING'
        ORDER BY d.createdAt ASC
        """)
    List<Delivery> findPendingByOriginDepot(@Param("depotId") UUID depotId);

    /**
     * Checks if a delivery number already exists
     * Used during delivery creation to ensure uniqueness
     */
    boolean existsByDeliveryNumber(String deliveryNumber);

    /**
     * Finds deliveries between two dates for reporting
     * Used for analytics and depot performance reports
     */
    @Query("""
        SELECT d FROM Delivery d
        WHERE d.originDepot.id = :depotId
          AND d.createdAt BETWEEN :startDate AND :endDate
        """)
    List<Delivery> findByDepotAndDateRange(
            @Param("depotId") UUID depotId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
