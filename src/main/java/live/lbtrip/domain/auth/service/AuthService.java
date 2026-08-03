package live.lbtrip.domain.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.auth.dto.request.LoginRequest;
import live.lbtrip.domain.auth.dto.request.SignupRequest;
import live.lbtrip.domain.auth.dto.request.TokenRefreshRequest;
import live.lbtrip.domain.auth.dto.response.LoginResponse;
import live.lbtrip.domain.auth.dto.response.SignupResponse;
import live.lbtrip.domain.auth.dto.response.TokenResponse;
import live.lbtrip.domain.auth.model.RefreshToken;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final Duration withdrawalGracePeriod;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        EmailVerificationService emailVerificationService,
        JwtTokenProvider jwtTokenProvider,
        RefreshTokenService refreshTokenService,
        @Value("${app.withdrawal.grace-period}") Duration withdrawalGracePeriod
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.withdrawalGracePeriod = withdrawalGracePeriod;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = StringNormalizer.trimToLowerCase(request.email());
        validateEmailNotDuplicated(email);

        User user = User.create(
            StringNormalizer.trim(request.name()),
            email,
            passwordEncoder.encode(request.password()),
            request.birthDate(),
            request.gender(),
            request.termsAgreed(),
            request.privacyAgreed(),
            request.marketingAgreed()
        );

        User savedUser = userRepository.save(user);
        long verificationCodeExpiresIn = emailVerificationService.issue(savedUser);
        return SignupResponse.from(savedUser, verificationCodeExpiresIn);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = StringNormalizer.trimToLowerCase(request.email());
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_LOGIN_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw BusinessException.of(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        boolean reinstated = false;
        if (user.isWithdrawn()) {
            user.reinstate(LocalDateTime.now(), withdrawalGracePeriod);
            reinstated = true;
        }
        if (!user.isActive()) {
            throw BusinessException.of(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = refreshTokenService.issue(user);

        return LoginResponse.of(accessToken, refreshToken, reinstated);
    }

    @Transactional
    public TokenResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.findUsable(request.refreshToken());
        User user = refreshToken.getUser();

        String newAccessToken = jwtTokenProvider.createAccessToken(user);

        return TokenResponse.of(newAccessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    private void validateEmailNotDuplicated(String email) {
        if (userRepository.existsByEmail(email)) {
            throw BusinessException.of(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
