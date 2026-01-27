package com.courigistics.courigisticsbackend.services.email;

/**
 * Email service for sending transactional emails
 */
public interface EmailService {
    /**
     * Sends email verification link to customer
     */
    void sendCustomerVerificationEmail(String to, String token);

    /**
     * Sends email verification link to courier applicant
     */
    void sendCourierVerificationEmail(String to, String token, String firstName);

    /**
     * Sends "pending approval" notification to courier after email verification
     */
    void sendCourierPendingApprovalEmail(String to, String firstName);

    /**
     * Sends approval notification with employee ID and setup link
     */
    void sendCourierApprovalEmail(String to, String firstName, String employeeId, String setUpToken);

    /**
     * Sends "account ready" confirmation after courier sets up username/password
     */
    void sendCourierAccountReadyEmail(String to, String firstName, String username);

    /**
     * Sends password reset link
     */
    void sendPasswordResetEmail(String to, String token);
}
