package live.lbtrip.domain.auth.service;

import java.util.Locale;

import live.lbtrip.domain.auth.dto.LoginRequest;
import live.lbtrip.domain.auth.dto.LoginResponse;
import live.lbtrip.domain.auth.dto.LogoutRequest;
import live.lbtrip.domain.auth.dto.SignupRequest;
import live.lbtrip.domain.auth.dto.SignupResponse;
import live.lbtrip.domain.auth.dto.TokenRefreshRequest;
import live.lbtrip.domain.auth.dto.TokenResponse;
import live.lbtrip.domain.auth.model.RefreshToken;
import live.lbtrip.domain.auth.repository.RefreshTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationService emailVerificationService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		EmailVerificationService emailVerificationService,
		JwtTokenProvider jwtTokenProvider,
		RefreshTokenRepository refreshTokenRepository
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailVerificationService = emailVerificationService;
		this.jwtTokenProvider = jwtTokenProvider;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		validateSignupRequest(request);

		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw BusinessException.of(ErrorCode.DUPLICATE_EMAIL);
		}

		User user = User.create(
			request.name().trim(),
			email,
			passwordEncoder.encode(request.password()),
			request.phoneNumber().trim(),
			request.age(),
			request.gender(),
			request.termsAgreed(),
			request.privacyAgreed(),
			request.marketingAgreed()
		);

		User savedUser = userRepository.save(user);
		emailVerificationService.issue(savedUser);
		return SignupResponse.from(savedUser);
	}

	@Transactional
	public LoginResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_LOGIN_CREDENTIALS));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw BusinessException.of(ErrorCode.INVALID_LOGIN_CREDENTIALS);
		}
		if (user.getStatus() != UserStatus.ACTIVE) {
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

		return LoginResponse.of(user, accessToken, refreshToken.getToken(), jwtTokenProvider.accessTokenExpiresIn());
	}

	@Transactional
	public TokenResponse refreshToken(TokenRefreshRequest request) {
		RefreshToken refreshToken = findUsableRefreshToken(request.refreshToken());
		User user = refreshToken.getUser();

		refreshToken.revoke();

		String newAccessToken = jwtTokenProvider.createAccessToken(user);
		RefreshToken newRefreshToken = RefreshToken.create(
			user,
			jwtTokenProvider.createRefreshToken(user),
			jwtTokenProvider.refreshTokenExpiresAt()
		);
		refreshTokenRepository.save(newRefreshToken);

		return TokenResponse.of(newAccessToken, newRefreshToken.getToken(), jwtTokenProvider.accessTokenExpiresIn());
	}

	@Transactional
	public void logout(LogoutRequest request) {
		RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken().trim())
			.orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_REFRESH_TOKEN));

		refreshToken.revoke();
	}

	private RefreshToken findUsableRefreshToken(String token) {
		RefreshToken refreshToken = refreshTokenRepository.findByToken(token.trim())
			.orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_REFRESH_TOKEN));

		if (refreshToken.isRevoked() || !jwtTokenProvider.isValid(refreshToken.getToken())) {
			throw BusinessException.of(ErrorCode.INVALID_REFRESH_TOKEN);
		}
		if (refreshToken.isExpired(java.time.Instant.now())) {
			throw BusinessException.of(ErrorCode.EXPIRED_REFRESH_TOKEN);
		}

		return refreshToken;
	}

	private void validateSignupRequest(SignupRequest request) {
		if (!request.password().equals(request.passwordConfirm())) {
			throw BusinessException.of(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
		}
		if (!request.termsAgreed() || !request.privacyAgreed()) {
			throw BusinessException.of(ErrorCode.REQUIRED_AGREEMENT_NOT_ACCEPTED);
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
