package com.courigistics.courigisticsbackend.services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.concurrent.CompletableFuture;

/**
 * Email service implementation using Thymeleaf templates and JavaMailSender
 * Handles all email communications for the CouriGistics platform
 *
 * All email operations are asynchronous using virtual threads, allowing
 * non-blocking email sending that can handle high volumes efficiently.
 *
 * @see com.courigistics.courigisticsbackend.config.EmailConfig for async configuration
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.application.frontend-url}")
    private String frontendUrl;

    /**
     * Sends courier verification email asynchronously
     * Uses virtual thread for non-blocking operation
     *
     * @param to Recipient email address
     * @param token Email verification token
     * @param firstName Courier first name
     * @return CompletableFuture that completes when email is sent
     */
    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<Void> sendCourierVerificationEmail(String to, String token, String firstName) {
        return CompletableFuture.runAsync(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("[{}] Sending courier verification email to: {}", threadName, to);

            try {
                // Build verification link
                String verificationLink = frontendUrl + "/courier/verify?token=" + token;

                // Create Thymeleaf context with variables
                Context context = new Context();
                context.setVariable("firstName", firstName);
                context.setVariable("verificationLink", verificationLink);

                // Process template
                String htmlContent = templateEngine.process("emails/courier-verification", context);

                // Send email
                sendHtmlEmail(to, "Verify Your Courier Application - CouriGistics", htmlContent);

                log.info("[{}] Courier verification email sent successfully to: {}", threadName, to);
            } catch (Exception e) {
                log.error("[{}] Failed to send courier verification email to: {}", threadName, to, e);
                throw new RuntimeException("Failed to send verification email", e);
            }
        });
    }

    /**
     * Sends pending approval notification asynchronously
     *
     * @param to Recipient email address
     * @param firstName Courier first name
     * @return CompletableFuture that completes when email is sent
     */
    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<Void> sendCourierPendingApprovalEmail(String to, String firstName) {
        return CompletableFuture.runAsync(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("[{}] Sending pending approval email to: {}", threadName, to);

            try {
                Context context = new Context();
                context.setVariable("firstName", firstName);

                String htmlContent = templateEngine.process("emails/courier-pending-approval", context);

                sendHtmlEmail(to, "Application Pending Review - Courigistics", htmlContent);

                log.info("[{}] Pending approval email sent successfully to: {}", threadName, to);
            } catch (Exception e) {
                log.error("[{}] Failed to send pending approval email to: {}", threadName, to, e);
                throw new RuntimeException("Failed to send pending approval email", e);
            }
        });
    }

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<Void> sendCourierApprovalEmail(String to, String firstName, String employeeId, String setupToken) {
        return CompletableFuture.runAsync(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("[{}] Sending courier approval email to: {} with employeeId: {}", threadName, to, employeeId);

            try {
                // Build setup link
                String setupLink = frontendUrl + "/courier/setup-account?token=" + setupToken;

                Context context = new Context();
                context.setVariable("firstName", firstName);
                context.setVariable("employeeId", employeeId);
                context.setVariable("setupLink", setupLink);

                String htmlContent = templateEngine.process("emails/courier-approved", context);

                sendHtmlEmail(to, "🎉 Application Approved - Set Up Your Account", htmlContent);

                log.info("[{}] Courier approval email sent successfully to: {}", threadName, to);
            } catch (Exception e) {
                log.error("[{}] Failed to send courier approval email to: {}", threadName, to, e);
                throw new RuntimeException("Failed to send approval email", e);
            }
        });
    }

    /**
     * Sends account ready notification asynchronously
     *
     * @param to Recipient email address
     * @param firstName Courier first name
     * @param username Created username
     * @return CompletableFuture that completes when email is sent
     */
    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<Void> sendCourierAccountReadyEmail(String to, String firstName, String username) {
        return CompletableFuture.runAsync(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("[{}] Sending account ready email to: {} with username: {}", threadName, to, username);

            try{
                Context context = new Context();
                context.setVariable("firstName", firstName);
                context.setVariable("username", username);

                String htmlContent = templateEngine.process("emails/courier-account-ready", context);

                sendHtmlEmail(to, "Your Courier Account is Ready! - CouriGistics", htmlContent);

                log.info("[{}] Account ready email sent successfully to: {}", threadName, to);
            } catch (Exception e){
                log.error("[{}] Failed to send account ready email to: {}", threadName, to, e);
                throw new RuntimeException("Failed to send account ready email", e);
            }
        });
    }

    /**
     * Sends customer verification email asynchronously
     *
     * @param to Recipient email address
     * @param token Email verification token
     * @param firstName Customer first name
     * @return CompletableFuture that completes when email is sent
     */
    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<Void> sendCustomerVerificationEmail(String to, String token, String firstName) {
        return CompletableFuture.runAsync(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("[{}] Sending customer verification email to: {}", threadName, to);

            try{
                String verificationLink = frontendUrl + "/verify?token=" + token;

                Context context = new Context();
                context.setVariable("firstName", firstName);
                context.setVariable("verificationLink", verificationLink);

                String htmlContent = templateEngine.process("emails/customer-verification", context);

                sendHtmlEmail(to, "Verify your Email - Courigistics", htmlContent);

                log.info("[{}] Customer verification email sent successfully to: {}", threadName, to);
            }catch (Exception e){
                log.error("[{}] Failed to send customer verification email to: {}", threadName, to, e);
                throw new RuntimeException("Failed to send verification email", e);
            }
        });
    }

    /**
     * Sends password reset email asynchronously
     *
     * @param to Recipient email address
     * @param token Password reset token
     * @param firstName User first name
     * @return CompletableFuture that completes when email is sent
     */
    @Override
    public CompletableFuture<Void> sendPasswordResetEmail(String to, String token, String firstName) {
        return CompletableFuture.runAsync(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("[{}] sending password reset email to: {}", threadName, to);

            try{
                String resetLink = frontendUrl + "/reset-password?token=" + token;

                Context context = new Context();
                context.setVariable("firstName", firstName);
                context.setVariable("resetLink", resetLink);

                String htmlContent = templateEngine.process("emails/password-reset",  context);

                sendHtmlEmail(to, "Password Reset Request - CouriGistics", htmlContent);
                log.info("[{}] Password reset email sent successfully to: {}", threadName, to);
            } catch (Exception e){
                log.error("[{}] Failed to send password reset email to: {}", threadName, to, e);
                throw new RuntimeException("Failed to send reset email", e);
            }
        });
    }


    /**
     * Helper method to send HTML emails
     *
     * Note: This method is synchronous and called from within async context.
     * The actual email sending blocks, but it's okay because we're on a virtual thread.
     * Virtual threads are designed for blocking I/O operations.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML email body
     * @throws MessagingException if email sending fails
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML email

        // This call blocks, but we're on a virtual thread so it's fine
        // Virtual threads are parked during I/O, freeing up platform threads
        mailSender.send(message);
    }
}
