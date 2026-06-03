package live.lbtrip.domain.auth.controller;

import jakarta.validation.Valid;
import live.lbtrip.domain.auth.dto.EmailVerificationConfirmRequest;
import live.lbtrip.domain.auth.dto.EmailVerificationResendRequest;
import live.lbtrip.domain.auth.dto.EmailVerificationResponse;
import live.lbtrip.domain.auth.dto.LoginRequest;
import live.lbtrip.domain.auth.dto.LoginResponse;
import live.lbtrip.domain.auth.dto.LogoutRequest;
import live.lbtrip.domain.auth.dto.TokenRefreshRequest;
import live.lbtrip.domain.auth.dto.TokenResponse;
import live.lbtrip.domain.auth.dto.SignupRequest;
import live.lbtrip.domain.auth.dto.SignupResponse;
import live.lbtrip.domain.auth.service.AuthService;
import live.lbtrip.domain.auth.service.EmailVerificationService;
import live.lbtrip.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final EmailVerificationService emailVerificationService;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
		SignupResponse response = authService.signup(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/token/refresh")
	public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
		TokenResponse response = authService.refreshToken(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Object>> logout(@Valid @RequestBody LogoutRequest request) {
		authService.logout(request);
		return ResponseEntity.ok(ApiResponse.success());
	}

	@PostMapping("/email-verifications/confirm")
	public ResponseEntity<ApiResponse<EmailVerificationResponse>> confirmEmailVerification(
		@Valid @RequestBody EmailVerificationConfirmRequest request
	) {
		EmailVerificationResponse response = emailVerificationService.confirm(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/email-verifications/resend")
	public ResponseEntity<ApiResponse<EmailVerificationResponse>> resendEmailVerification(
		@Valid @RequestBody EmailVerificationResendRequest request
	) {
		EmailVerificationResponse response = emailVerificationService.resend(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
