package com.talentpulse.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.talentpulse.auth.config.JwtProperties;
import com.talentpulse.auth.dto.AuthResponse;
import com.talentpulse.auth.dto.CandidateRegisterRequest;
import com.talentpulse.auth.entity.User;
import com.talentpulse.auth.enums.Role;
import com.talentpulse.auth.event.DomainEventPublisher;
import com.talentpulse.auth.exception.EmailAlreadyExistsException;
import com.talentpulse.auth.repository.OrganizationRepository;
import com.talentpulse.auth.repository.PasswordResetTokenRepository;
import com.talentpulse.auth.repository.RefreshTokenRepository;
import com.talentpulse.auth.repository.UserRepository;
import com.talentpulse.auth.security.JwtService;
import com.talentpulse.auth.security.TokenHashService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Fast unit tests — AuthService only, DB is mocked.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private TokenHashService tokenHashService;
    @Mock private JwtProperties jwtProperties;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private DomainEventPublisher eventPublisher;

    @InjectMocks
    private AuthService authService;

    private CandidateRegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new CandidateRegisterRequest();
        request.setFullName("Riya Shah");
        request.setEmail("riya@test.com");
        request.setPassword("Secret@123");
    }

    @Test
    void registerCandidate_success_returnsTokens() {
        when(userRepository.existsByEmail("riya@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(jwtService.generateAccessToken(any(), anyString(), any(Role.class), nullable(UUID.class)))
                .thenReturn("access-token");
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(900L);
        when(tokenHashService.hash(anyString())).thenReturn("refresh-hash");
        when(jwtProperties.getRefreshTokenExpiryDays()).thenReturn(7L);
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.registerCandidate(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getExpiresIn()).isEqualTo(900L);
        assertThat(response.getUser().getEmail()).isEqualTo("riya@test.com");
        assertThat(response.getUser().getRole()).isEqualTo(Role.CANDIDATE);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.CANDIDATE);
    }

    @Test
    void registerCandidate_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("riya@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerCandidate(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }
}
