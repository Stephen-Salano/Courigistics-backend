package com.courigistics.courigisticsbackend.dto.responses.courier;

/**
 * Response after successful courier registration
 * Informs user that verification email has been sent
 */
public record CourierRegistrationResponse(
        String message,
        String email,
        String nextStep
) {
    public static CourierRegistrationResponse success(String email){
        return new CourierRegistrationResponse(
                "Registration successful! please check your email to verify your account. ",
                email,
                "Check your email for a verification link. After verification, your application will be reviewed" +
                        "by our admin team"
        );
    }
}
