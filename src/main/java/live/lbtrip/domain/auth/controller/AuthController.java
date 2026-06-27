package live.lbtrip.domain.auth.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import live.lbtrip.domain.auth.dto.request.EmailVerificationConfirmRequest;
import live.lbtrip.domain.auth.dto.request.EmailVerificationResendRequest;
import live.lbtrip.domain.auth.dto.request.LoginRequest;
import live.lbtrip.domain.auth.dto.request.SignupRequest;
import live.lbtrip.domain.auth.dto.request.TokenRefreshRequest;
import live.lbtrip.domain.auth.dto.response.EmailVerificationResponse;
import live.lbtrip.domain.auth.dto.response.LoginResponse;
import live.lbtrip.domain.auth.dto.response.SignupResponse;
import live.lbtrip.domain.auth.dto.response.TokenResponse;
import live.lbtrip.domain.auth.service.AuthService;
import live.lbtrip.domain.auth.service.EmailVerificationService;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

	private final AuthService authService;
	private final EmailVerificationService emailVerificationService;

	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
		SignupResponse response = authService.signup(request);
		return ResponseEntity.status(CREATED).body(response);
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/refresh")
	public TokenResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
		return authService.refreshToken(request);
	}

	@PostMapping("/logout")
	public void logout(@UserId Long userId) {
		authService.logout(userId);
	}

	@PostMapping("/email-verifications/confirm")
	public EmailVerificationResponse confirmEmailVerification(
		@Valid @RequestBody EmailVerificationConfirmRequest request
	) {
		return emailVerificationService.confirm(request);
	}

	@PostMapping("/email-verifications/resend")
	public EmailVerificationResponse resendEmailVerification(
		@Valid @RequestBody EmailVerificationResendRequest request
	) {
		return emailVerificationService.resend(request);
	}
}
