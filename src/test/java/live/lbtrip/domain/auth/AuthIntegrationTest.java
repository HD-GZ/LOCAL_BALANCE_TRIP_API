package live.lbtrip.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import live.lbtrip.domain.auth.dto.EmailVerificationConfirmRequest;
import live.lbtrip.domain.auth.dto.LoginRequest;
import live.lbtrip.domain.auth.dto.LoginResponse;
import live.lbtrip.domain.auth.dto.LogoutRequest;
import live.lbtrip.domain.auth.dto.SignupRequest;
import live.lbtrip.domain.auth.dto.SignupResponse;
import live.lbtrip.domain.auth.dto.TokenRefreshRequest;
import live.lbtrip.domain.auth.dto.TokenResponse;
import live.lbtrip.domain.auth.repository.EmailVerificationTokenRepository;
import live.lbtrip.domain.auth.repository.RefreshTokenRepository;
import live.lbtrip.domain.auth.service.AuthService;
import live.lbtrip.domain.auth.service.EmailVerificationService;
import live.lbtrip.domain.user.model.Gender;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest
class AuthIntegrationTest {

	@Autowired
	private AuthService authService;

	@Autowired
	private EmailVerificationService emailVerificationService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmailVerificationTokenRepository emailVerificationTokenRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@MockitoBean
	private JavaMailSender mailSender;

	@BeforeEach
	void setUp() {
		refreshTokenRepository.deleteAll();
		emailVerificationTokenRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void signupCreatesPendingUserAndSendsVerificationEmail() {
		SignupResponse response = authService.signup(signupRequest("local@email.com"));

		assertThat(response.status()).isEqualTo(UserStatus.PENDING_EMAIL_VERIFICATION);
		assertThat(userRepository.existsByEmail("local@email.com")).isTrue();
		assertThat(emailVerificationTokenRepository.findAll()).hasSize(1);
		verify(mailSender).send(any(SimpleMailMessage.class));
	}

	@Test
	void signupRejectsDuplicateEmail() {
		authService.signup(signupRequest("local@email.com"));

		assertThatThrownBy(() -> authService.signup(signupRequest("LOCAL@email.com")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DUPLICATE_EMAIL);
	}

	@Test
	void emailVerificationActivatesUser() {
		authService.signup(signupRequest("local@email.com"));
		String token = emailVerificationTokenRepository.findAll().get(0).getToken();

		emailVerificationService.confirm(new EmailVerificationConfirmRequest(token));

		User user = userRepository.findByEmail("local@email.com").orElseThrow();
		assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(emailVerificationTokenRepository.findByToken(token).orElseThrow().isUsed()).isTrue();
	}

	@Test
	void loginRejectsUnverifiedUser() {
		authService.signup(signupRequest("local@email.com"));

		assertThatThrownBy(() -> authService.login(new LoginRequest("local@email.com", "password1")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
	}

	@Test
	void loginRefreshAndLogoutManageTokens() {
		authService.signup(signupRequest("local@email.com"));
		String verificationToken = emailVerificationTokenRepository.findAll().get(0).getToken();
		emailVerificationService.confirm(new EmailVerificationConfirmRequest(verificationToken));

		LoginResponse loginResponse = authService.login(new LoginRequest("local@email.com", "password1"));
		TokenResponse tokenResponse = authService.refreshToken(new TokenRefreshRequest(loginResponse.refreshToken()));
		authService.logout(new LogoutRequest(tokenResponse.refreshToken()));

		assertThat(loginResponse.accessToken()).isNotBlank();
		assertThat(loginResponse.refreshToken()).isNotBlank();
		assertThat(tokenResponse.accessToken()).isNotBlank();
		assertThat(tokenResponse.refreshToken()).isNotEqualTo(loginResponse.refreshToken());
		assertThat(refreshTokenRepository.findByToken(loginResponse.refreshToken()).orElseThrow().isRevoked()).isTrue();
		assertThat(refreshTokenRepository.findByToken(tokenResponse.refreshToken()).orElseThrow().isRevoked()).isTrue();
	}

	private SignupRequest signupRequest(String email) {
		return new SignupRequest(
			"홍길동",
			email,
			"password1",
			"password1",
			"010-1234-5678",
			25,
			Gender.NOT_SPECIFIED,
			true,
			true,
			false
		);
	}
}
