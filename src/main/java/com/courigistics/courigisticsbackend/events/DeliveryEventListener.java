package com.courigistics.courigisticsbackend.events;

import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.Customer;
import com.courigistics.courigisticsbackend.entities.Delivery;
import com.courigistics.courigisticsbackend.services.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for delivery-related lifecycle events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventListener {

    private final EmailService emailService;

    /**
     * Handles DeliveryAssignedEvent by notifying the customer via email.
     *
     * Marks the listener as @Async to prevent blocking the assignment transaction
     * during email generation and network I/O.
     */
    @Async
    @EventListener
    public void handleDeliveryAssignedEvent(DeliveryAssignedEvent event) {
        Delivery delivery = event.getDelivery();
        Courier courier = event.getCourier();
        Account senderAccount = delivery.getSender();

        log.info("Handling DeliveryAssignedEvent for delivery: {} and courier: {}",
                delivery.getDeliveryNumber(), courier.getFirstName());

        // We use the sender's account to notify them
        // Note: In a production environment, we might want to ensure the sender has a profile
        String firstName = (senderAccount.getCustomer() != null)
                ? senderAccount.getCustomer().getFirstName()
                : "Customer";

        emailService.sendDeliveryAssignedEmail(
                senderAccount.getEmail(),
                firstName,
                delivery.getDeliveryNumber(),
                courier.getFirstName() + " " + courier.getLastName()
        );
    }
}
