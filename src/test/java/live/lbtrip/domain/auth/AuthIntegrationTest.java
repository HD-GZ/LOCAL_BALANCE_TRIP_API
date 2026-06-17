package live.lbtrip.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import live.lbtrip.domain.auth.dto.request.EmailVerificationConfirmRequest;
import live.lbtrip.domain.auth.dto.request.LoginRequest;
import live.lbtrip.domain.auth.dto.request.SignupRequest;
import live.lbtrip.domain.auth.dto.request.TokenRefreshRequest;
import live.lbtrip.domain.auth.dto.response.LoginResponse;
import live.lbtrip.domain.auth.dto.response.SignupResponse;
import live.lbtrip.domain.auth.dto.response.TokenResponse;
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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
	private SendGrid sendGrid;

	@BeforeEach
	void setUp() throws IOException {
		when(sendGrid.api(any(Request.class))).thenReturn(new Response(202, "", Map.of()));
		refreshTokenRepository.deleteAll();
		emailVerificationTokenRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void signupCreatesPendingUserAndSendsVerificationEmail() throws IOException {
		SignupResponse response = authService.signup(signupRequest("local@email.com"));

		assertThat(response.status()).isEqualTo(UserStatus.PENDING_EMAIL_VERIFICATION);
		assertThat(userRepository.existsByEmail("local@email.com")).isTrue();
		assertThat(emailVerificationTokenRepository.findAll()).hasSize(1);

		ArgumentCaptor<Request> requestCaptor = forClass(Request.class);
		verify(sendGrid).api(requestCaptor.capture());
		assertThat(requestCaptor.getValue().getBody()).contains("local@email.com");
		assertThat(requestCaptor.getValue().getBody()).containsPattern("인증 코드: \\d{6}");
	}

	@Test
	void signupRollsBackWhenSendGridFails() throws IOException {
		when(sendGrid.api(any(Request.class))).thenReturn(new Response(500, "send failed", Map.of()));

		assertThatThrownBy(() -> authService.signup(signupRequest("local@email.com")))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("SendGrid email send failed");

		assertThat(userRepository.existsByEmail("local@email.com")).isFalse();
		assertThat(emailVerificationTokenRepository.findAll()).isEmpty();
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
		String code = emailVerificationTokenRepository.findAll().get(0).getCode();

		emailVerificationService.confirm(new EmailVerificationConfirmRequest(code));

		User user = userRepository.findByEmail("local@email.com").orElseThrow();
		assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(emailVerificationTokenRepository.findByCode(code).orElseThrow().isUsed()).isTrue();
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
		String verificationCode = emailVerificationTokenRepository.findAll().get(0).getCode();
		emailVerificationService.confirm(new EmailVerificationConfirmRequest(verificationCode));

		LoginResponse loginResponse = authService.login(new LoginRequest("local@email.com", "password1"));
		TokenResponse tokenResponse = authService.refreshToken(new TokenRefreshRequest(loginResponse.refreshToken()));
		TokenResponse reusedTokenResponse = authService.refreshToken(new TokenRefreshRequest(loginResponse.refreshToken()));

		assertThat(loginResponse.accessToken()).isNotBlank();
		assertThat(loginResponse.refreshToken()).isNotBlank();
		assertThat(tokenResponse.accessToken()).isNotBlank();
		assertThat(tokenResponse.refreshToken()).isEqualTo(loginResponse.refreshToken());
		assertThat(reusedTokenResponse.accessToken()).isNotBlank();
		assertThat(reusedTokenResponse.refreshToken()).isEqualTo(loginResponse.refreshToken());
		assertThat(refreshTokenRepository.findByToken(loginResponse.refreshToken())).isPresent();

		User user = userRepository.findByEmail("local@email.com").orElseThrow();
		authService.logout(user.getId());

		assertThat(refreshTokenRepository.findByToken(loginResponse.refreshToken())).isEmpty();
	}

	private SignupRequest signupRequest(String email) {
		return new SignupRequest(
			"홍길동",
			email,
			"password1",
			"password1",
			LocalDate.of(1999, 1, 1),
			Gender.NOT_SPECIFIED,
			true,
			true,
			false
		);
	}
}
