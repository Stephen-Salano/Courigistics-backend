package com.courigistics.courigisticsbackend.events;

import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.services.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventListener {

    private final EmailService emailService;

    @EventListener
    public void handleRegistrationCompleteEvent(OnRegistrationCompleteEvent event) {
        log.info("Handling registration complete event for email: {}", event.getAccount().getEmail());

        Account account = event.getAccount();
        String token = event.getToken();

        // Determine user type and send the correct email
        switch (account.getAccountType()) {
            case CUSTOMER:
                emailService.sendCustomerVerificationEmail(account.getEmail(), token, account.getCustomer().getFirstName());
                break;
            case COURIER:
                // This is handled directly in the CourierAuthServiceImpl for now to manage the complex flow.
                // If we wanted to unify, we would move the courier email logic here.
                log.info("Courier registration event triggered, but email is sent via CourierAuthServiceImpl.");
                break;
            default:
                log.warn("Registration event for unhandled account type: {}", account.getAccountType());
                break;
        }
    }
}
