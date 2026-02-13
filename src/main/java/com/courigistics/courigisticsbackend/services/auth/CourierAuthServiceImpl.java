package com.courigistics.courigisticsbackend.services.auth;

import com.courigistics.courigisticsbackend.config.security.JwtService;
import com.courigistics.courigisticsbackend.dto.requests.auth.LoginRequest;
import com.courigistics.courigisticsbackend.dto.requests.courier.CourierRegisterRequest;
import com.courigistics.courigisticsbackend.dto.requests.courier.CourierSetupAccountRequest;
import com.courigistics.courigisticsbackend.dto.responses.auth.AuthResponse;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierRegistrationResponse;
import com.courigistics.courigisticsbackend.entities.*;
import com.courigistics.courigisticsbackend.entities.enums.*;
import com.courigistics.courigisticsbackend.exceptions.BadRequestException;
import com.courigistics.courigisticsbackend.exceptions.DuplicateResourceException;
import com.courigistics.courigisticsbackend.exceptions.ResourceNotFoundException;
import com.courigistics.courigisticsbackend.repositories.*;
import com.courigistics.courigisticsbackend.services.email.EmailService;
import com.courigistics.courigisticsbackend.services.verification_token.VerificationTokenService;
import com.courigistics.courigisticsbackend.utils.CourierValidationUtils;
import com.courigistics.courigisticsbackend.utils.EmployeeIdGenerator;
import com.courigistics.courigisticsbackend.utils.PhoneNumberUtils;
import com.courigistics.courigisticsbackend.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service("courierAuthService")
@Slf4j
@RequiredArgsConstructor
public class CourierAuthServiceImpl implements CourierAuthService {
    private final CourierRepository courierRepository;
    private final AccountRepository accountRepository;
    private final VehicleRepository vehicleRepository;
    private final DepotRepository depotRepository;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeIdGenerator employeeIdGenerator;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.courier.default-depot.code}")
    private String defaultDepotCode;

    @Value("${app.courier.approval.auto-approve}")
    private boolean autoApprove;

    @Override
    public CourierRegistrationResponse registerCourier(CourierRegisterRequest request) {
        log.info("Starting courier registration for email: {}", request.email());

        // 1. Normalize phone number
        String normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(request.phone());

        // we validate the request
        CourierValidationUtils.validateVehicleRequired(request.employmentType(), request.vehicleDetails());
        CourierValidationUtils.validateLicenseNotExpired(request.licenseExpiryDate());

        // 2: Check duplicates
        if (accountRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException("Email already registered");
        }
        if (accountRepository.existsByPhone(normalizedPhone)){
            throw new DuplicateResourceException("Phone number already registered");
        }

        if (courierRepository.existsByDriversLicenseNumber(request.driversLicenseNumber())){
            throw new DuplicateResourceException("Driver's license number already registered");
        }
        if (courierRepository.existsByNationalId(request.nationalId())){
            throw new DuplicateResourceException("National ID already registered");
        }
        if (request.vehicleDetails() != null && vehicleRepository.existsByLicencePlate(request.vehicleDetails().licensePlate())){
            throw new DuplicateResourceException("Vehicle license plate already registered");
        }

        // 3: Find the default depot (Only if NOT a freelancer)
        Depot assignedDepot = null;
        if (request.employmentType() != EmploymentType.FREELANCER) {
            assignedDepot = depotRepository.findByCode(defaultDepotCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Default depot not found"));
        }

        Account account = Account.builder()
                .email(request.email())
                .phone(normalizedPhone) // Use normalized phone
                .username(null) // will be set during account setup
                .password(null) // Set during account setup
                .accountType(AccountType.COURIER)
                .enabled(false)
                .emailVerified(false)
                .accountNonLocked(true)
                .build();

        account = accountRepository.save(account);
        log.info("Created account for courier: {}", account.getEmail());

        // 5: Create Courier
        Courier courier = Courier.builder()
                .account(account)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .nationalId(request.nationalId())
                .employmentType(request.employmentType())
                .driversLicenseNumber(request.driversLicenseNumber())
                .licenseExpiryDate(request.licenseExpiryDate())
                .depot(assignedDepot)
                .status(CourierStatus.PENDING)
                .pendingApproval(true)
                .build();
        courier = courierRepository.save(courier);
        log.info("Created courier profile: {} {}", courier.getFirstName(), courier.getLastName());

        // 6. Create vehicle patterns if freelancer
        if (request.employmentType() == EmploymentType.FREELANCER){
            Vehicles vehicles = Vehicles.builder()
                    .courier(courier)
                    .depot(assignedDepot) // This will be null for freelancers
                    .vehicleType(Objects.requireNonNull(request.vehicleDetails()).vehicleType())
                    .make(request.vehicleDetails().make())
                    .model(request.vehicleDetails().model())
                    .licencePlate(request.vehicleDetails().licensePlate())
                    .manufactureYear(String.valueOf(request.vehicleDetails().year()))
                    .vehicleColor(request.vehicleDetails().color())
                    .vehicleCapacityKg(request.vehicleDetails().capacityKg())
                    .vehicleCapacityM3(request.vehicleDetails().capacityM3())
                    .status(VehicleStatus.ACTIVE)
                    .build();

            vehicleRepository.save(vehicles);
            log.info("Created vehicle for freelancer: {}", vehicles.getLicencePlate());
        }

        // 7: Generate Verification Token
        VerificationToken verificationToken = verificationTokenService.createToken(account, TokenType.EMAIL_VERIFICATION);
        log.debug("Created verification token for courier: token={} expiresAt={}",
                verificationToken.getToken(), verificationToken.getExpiryDate()
        );

        emailService.sendCourierVerificationEmail(
                account.getEmail(), verificationToken.getToken(), courier.getFirstName()
        );

        log.info("Courier registration complete for: {}", request.email());

        return CourierRegistrationResponse.success(request.email());
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        log.info("Verifying courier email with token");

        // 1: Validate token
        VerificationToken verificationToken = verificationTokenService
                .validateToken(token, TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        // 2: Mark account email verified
        Account account = verificationToken.getAccount();
        account.setEmailVerified(true);
        accountRepository.save(account);

        // 3: Delete Tooken
        verificationTokenService.deleteToken(verificationToken);

        // 4: Find courier and update status
        Courier courier = courierRepository.findByAccount_Email(account.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));
        courier.setStatus(CourierStatus.PENDING); // Still pending admin approval
        courierRepository.save(courier);

        log.info("Email verified for courier: {}", account.getEmail());

        // If auto-approve is enabled (for testing), approve immediately
        if (autoApprove){
            log.info("Auto-approve enabled. Skipping 'Pending Approval' email to avoid rate-limiting and speed up flow.");
            approveCourier(courier.getId(), null);
        } else {
            // 5: Send "pending approval email" only if NOT auto-approving
            // In production, this is the only email sent at this stage.
            emailService.sendCourierPendingApprovalEmail(account.getEmail(), courier.getFirstName());
        }
        return true;
    }

    @Override
    @Transactional
    public void approveCourier(UUID courierId, UUID adminId) {
        log.info("Admin approving courier: {}", courierId);

        // Find courier
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));

        // 2 Verify email is verified
        if (!Boolean.TRUE.equals(courier.getAccount().getEmailVerified())){
            throw new BadRequestException("Courier email not yet verified");
        }

        // 3. Handle Admin (can be null for auto-approve)
        Account admin = null;
        if (adminId != null) {
            admin = accountRepository.findById(adminId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        }

        // 4: Update courier
        courier.setPendingApproval(false);
        courier.setApprovedAt(LocalDateTime.now());
        courier.setStatus(CourierStatus.ACTIVE);
        courier.setApprovedBy(admin);

        // 5: Generate account setup token
        VerificationToken setUpToken = verificationTokenService.createToken(
                courier.getAccount(), TokenType.ACCOUNT_SETUP
        );
        log.debug("Created setup token for courier: token={} expiresAt={}",
                setUpToken.getToken(), setUpToken.getExpiryDate()
        );

        // 6: Handle specific logic based on employment type
        if (courier.getEmploymentType() == EmploymentType.EMPLOYEE) {
            String employeeId = employeeIdGenerator.generateEmployeeId();
            courier.setEmployeeId(employeeId);
            courierRepository.save(courier);

            emailService.sendCourierEmployeeApprovalEmail(
                courier.getAccount().getEmail(),
                courier.getFirstName(),
                employeeId,
                setUpToken.getToken()
            );
        } else {
            courierRepository.save(courier);

            emailService.sendCourierFreelancerApprovalEmail(
                    courier.getAccount().getEmail(),
                    courier.getFirstName(),
                    setUpToken.getToken()
            );
        }

        log.info("Courier approved: {}", courier.getAccount().getEmail());
    }

    @Override
    @Transactional
    public void setupAccount(CourierSetupAccountRequest request) {
        log.info("Setting up account with token");

        // 1: Validate passwords match
        ValidationUtils.validatePasswordsMatch(request.password(), request.confirmPassword());

        // 2: Validate token
        VerificationToken token = verificationTokenService.validateToken(request.token(), TokenType.ACCOUNT_SETUP)
                .orElseThrow(() -> new BadRequestException("Invalid or expired setup token"));

        Account account = token.getAccount();

        // 3: Find courier
        Courier courier = courierRepository.findByAccount_Email(account.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Courier profile not found"));

        // 4: Validate courier is approved
        CourierValidationUtils.validateCourierApproved(courier.getPendingApproval());

        // 5: Check username is available
        if (accountRepository.existsByUsername(request.username())){
            throw new BadRequestException("Username already taken");
        }

        // 6: Check account not already setup
        if (account.getUsername() != null){
            throw new BadRequestException("Account already setup");
        }

        // 7: Update account with username and password
        account.setUsername(request.username());
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setEnabled(true);
        accountRepository.save(account);

        // 8: Delete setup token
        verificationTokenService.deleteToken(token);

        // 9: Send account ready email
        emailService.sendCourierAccountReadyEmail(
                account.getEmail(),
                courier.getFirstName(),
                account.getUsername()
        );

        log.info("Account setup complete for : {} | username {}", account.getEmail(), account.getUsername());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Courier login attempt for : {}", request.usernameOrEmail());

        // 1: Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.usernameOrEmail(),
                        request.password()
                )
        );

        // 2: Get account
        Account account = (Account) authentication.getPrincipal();

        // 3: Verify account type is COURIER
        if (account.getAccountType() != AccountType.COURIER){
            throw new BadRequestException("Invalid account type for courier login");
        }

        // 4: Generate tokens
        String accessToken = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken(account);

        // Save the refresh token
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .account(account)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                .invalidated(false)
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        // 6: Update last login
        account.setLastLogin(LocalDateTime.now());
        accountRepository.save(account);

        log.info("Courier login successful: {}", account.getUsername());

        return AuthResponse.of(
                accessToken, refreshToken,
                jwtService.getAccessTokenExpiration(),
                account.getUsername(),
                account.getEmail(), account.getAccountType().name()
        );
    }
}
