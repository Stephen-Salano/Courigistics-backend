package com.courigistics.courigisticsbackend.entities.enums;

/**
 * Represents the approval workflow status for courier applications
 */
public enum ApprovalStatus {
    /**
     * Email not yet verified
     */
    PENDING_VERIFICATION,

    /**
     * Email verified, awaiting admin approval
     */
    PENDING_APPROVAL,

    /**
     * Admin approved the application
     */
    APPROVED,

    /**
     * Admin rejected the application
     */
    REJECTED
}
