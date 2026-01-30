package com.courigistics.courigisticsbackend.services.email;

import java.util.concurrent.CompletableFuture;

/**
 * Email service for sending transactional emails
 */
public interface EmailService {

    /**
     * Sends courier verification email asynchronously
     *
     * Use case: After courier registration, send email with verification link
     *
     * @param to Courier email address
     * @param token Email verification token (15-minute expiry)
     * @param firstName Courier first name for personalization
     * @return CompletableFuture that completes when email is sent (or fails)
     *
     * Example:
     * <pre>
     * emailService.sendCourierVerificationEmail("john@example.com", "abc123", "John")
     *     .thenRun(() -> log.info("Email sent"))
     *     .exceptionally(ex -> {
     *         log.error("Email failed", ex);
     *         return null;
     *     });
     * </pre>
     */
    CompletableFuture<Void> sendCourierVerificationEmail(String to, String token, String firstName);

    /**
     * Sends pending approval notification asynchronously
     *
     * Use case: After email verification, notify courier that admin review is pending
     *
     * @param to Courier email address
     * @param firstName Courier first name
     * @return CompletableFuture that completes when email is sent
     */
    CompletableFuture<Void> sendCourierPendingApprovalEmail(String to, String firstName);

    /**
     * Sends courier approval email with account setup link asynchronously
     *
     * Use case: After admin approval, send email with employee ID and setup link
     *
     * @param to Courier email address
     * @param firstName Courier first name
     * @param employeeId Generated employee ID (e.g., "COU-2025-0001")
     * @param setupToken Account setup token (7-day expiry)
     * @return CompletableFuture that completes when email is sent
     */
    CompletableFuture<Void> sendCourierApprovalEmail(String to, String firstName, String employeeId, String setupToken);

    /**
     * Sends account ready notification asynchronously
     *
     * Use case: After account setup complete, notify courier they can login
     *
     * @param to Courier email address
     * @param firstName Courier first name
     * @param username Created username
     * @return CompletableFuture that completes when email is sent
     */
    CompletableFuture<Void> sendCourierAccountReadyEmail(String to, String firstName, String username);

    /**
     * Sends customer verification email asynchronously
     *
     * Use case: After customer registration
     *
     * @param to Customer email address
     * @param token Email verification token
     * @param firstName Customer first name
     * @return CompletableFuture that completes when email is sent
     */
    CompletableFuture<Void> sendCustomerVerificationEmail(String to, String token, String firstName);

    /**
     * Sends password reset email asynchronously
     *
     * Use case: User requests password reset
     *
     * @param to User email address
     * @param token Password reset token
     * @param firstName User first name
     * @return CompletableFuture that completes when email is sent
     */
    CompletableFuture<Void> sendPasswordResetEmail(String to, String token, String firstName);
}
