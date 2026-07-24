package com.talentpulse.auth.service;

import com.talentpulse.auth.config.JwtProperties;
import com.talentpulse.auth.dto.AuthResponse;
import com.talentpulse.auth.dto.CandidateRegisterRequest;
import com.talentpulse.auth.dto.ForgotPasswordRequest;
import com.talentpulse.auth.dto.LoginRequest;
import com.talentpulse.auth.dto.MessageResponse;
import com.talentpulse.auth.dto.RecruiterRegisterRequest;
import com.talentpulse.auth.dto.RefreshTokenRequest;
import com.talentpulse.auth.dto.ResetPasswordRequest;
import com.talentpulse.auth.dto.UserMapper;
import com.talentpulse.auth.dto.UserResponse;
import com.talentpulse.auth.entity.Organization;
import com.talentpulse.auth.entity.PasswordResetToken;
import com.talentpulse.auth.entity.RefreshToken;
import com.talentpulse.auth.entity.User;
import com.talentpulse.auth.enums.OrganizationStatus;
import com.talentpulse.auth.enums.Role;
import com.talentpulse.auth.enums.UserStatus;
import com.talentpulse.auth.event.DomainEventPublisher;
import com.talentpulse.auth.event.EventKeys;
import com.talentpulse.auth.event.UserRegisteredEvent;
import com.talentpulse.auth.exception.EmailAlreadyExistsException;
import com.talentpulse.auth.exception.InvalidCredentialsException;
import com.talentpulse.auth.exception.InvalidTokenException;
import com.talentpulse.auth.exception.ResourceNotFoundException;
import com.talentpulse.auth.repository.OrganizationRepository;
import com.talentpulse.auth.repository.PasswordResetTokenRepository;
import com.talentpulse.auth.repository.RefreshTokenRepository;
import com.talentpulse.auth.repository.UserRepository;
import com.talentpulse.auth.security.AuthPrincipal;
import com.talentpulse.auth.security.JwtService;
import com.talentpulse.auth.security.TokenHashService;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Business logic for authentication.
 * Controllers stay thin — all rules live here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenHashService tokenHashService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final DomainEventPublisher eventPublisher;

    @Value("${talentpulse.app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AuthResponse registerCandidate(CandidateRegisterRequest request) {
        ensureEmailAvailable(request.getEmail());

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(normalizeEmail(request.getEmail()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CANDIDATE)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .organization(null)
                .build();

        userRepository.save(user);
        publishUserRegistered(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse registerRecruiter(RecruiterRegisterRequest request) {
        ensureEmailAvailable(request.getEmail());

        Organization organization = Organization.builder()
                .name(request.getOrganizationName().trim())
                .slug(generateUniqueSlug(request.getOrganizationName()))
                .status(OrganizationStatus.ACTIVE)
                .build();
        organizationRepository.save(organization);

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(normalizeEmail(request.getEmail()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.RECRUITER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .organization(organization)
                .build();

        userRepository.save(user);
        publishUserRegistered(user);
        return issueTokens(user);
    }

    private void publishUserRegistered(User user) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publish(EventKeys.USER_REGISTERED, event);
                }
            });
        } else {
            eventPublisher.publish(EventKeys.USER_REGISTERED, event);
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizeEmail(request.getEmail()),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = tokenHashService.hash(request.getRefreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }

        User user = stored.getUser();

        // Rotate refresh token (old one becomes invalid)
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(user);
    }

    @Transactional
    public MessageResponse logout(RefreshTokenRequest request) {
        String tokenHash = tokenHashService.hash(request.getRefreshToken());

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });

        return new MessageResponse("Logged out successfully");
    }

    @Transactional(readOnly = true)
    public UserResponse me(AuthPrincipal principal) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserMapper.toResponse(user);
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        // Always return same message (don't reveal if email exists)
        userRepository.findByEmail(email).ifPresent(user -> {
            String rawToken = generateSecureToken();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenHashService.hash(rawToken))
                    .expiresAt(Instant.now().plusSeconds(30 * 60)) // 30 minutes
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            // DEV: log a reset link (no OTP). In production, send this via email.
            String resetUrl = frontendUrl + "/reset-password?token=" + rawToken;
            log.info("Password reset link for {} => {}", email, resetUrl);
        });

        return new MessageResponse("If the email exists, a reset link has been sent");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String tokenHash = tokenHashService.hash(request.getToken());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Reset token expired or already used");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Force re-login on all devices
        refreshTokenRepository.revokeAllActiveTokensForUser(user.getId());

        return new MessageResponse("Password updated successfully");
    }

    private AuthResponse issueTokens(User user) {
        UUID organizationId = user.getOrganization() != null ? user.getOrganization().getId() : null;

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                organizationId
        );

        String rawRefreshToken = generateSecureToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHashService.hash(rawRefreshToken))
                .expiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpiryDays() * 24 * 60 * 60))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .user(UserMapper.toResponse(user))
                .build();
    }

    private void ensureEmailAvailable(String email) {
        if (userRepository.existsByEmail(normalizeEmail(email))) {
            throw new EmailAlreadyExistsException(normalizeEmail(email));
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateUniqueSlug(String organizationName) {
        String base = organizationName.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-)|(-$)", "");
        if (base.isBlank()) {
            base = "org";
        }

        String slug = base;
        int suffix = 1;
        while (organizationRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
