package live.lbtrip.domain.auth.service;

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
import live.lbtrip.domain.auth.repository.RefreshTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

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
        if (!user.isActive()) {
            throw BusinessException.of(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        RefreshToken refreshToken = RefreshToken.create(
            user,
            jwtTokenProvider.createRefreshToken(user),
            jwtTokenProvider.refreshTokenExpiresAt()
        );

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(refreshToken);

        return LoginResponse.of(accessToken, refreshToken.getToken());
    }

    @Transactional
    public TokenResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken refreshToken = findUsableRefreshToken(request.refreshToken());
        User user = refreshToken.getUser();

        String newAccessToken = jwtTokenProvider.createAccessToken(user);

        return TokenResponse.of(newAccessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private RefreshToken findUsableRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(StringNormalizer.trim(token))
            .orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!jwtTokenProvider.isValid(refreshToken.getToken())) {
            throw BusinessException.of(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        refreshToken.validateNotExpired(java.time.LocalDateTime.now());

        return refreshToken;
    }

    private void validateEmailNotDuplicated(String email) {
        if (userRepository.existsByEmail(email)) {
            throw BusinessException.of(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
