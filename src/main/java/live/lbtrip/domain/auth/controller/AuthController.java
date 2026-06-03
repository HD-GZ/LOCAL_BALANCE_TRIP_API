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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;
	private final EmailVerificationService emailVerificationService;

	public AuthController(AuthService authService, EmailVerificationService emailVerificationService) {
		this.authService = authService;
		this.emailVerificationService = emailVerificationService;
	}

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
		return ApiResponse.success(authService.signup(request));
	}

	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ApiResponse.success(authService.login(request));
	}

	@PostMapping("/token/refresh")
	public ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
		return ApiResponse.success(authService.refreshToken(request));
	}

	@PostMapping("/logout")
	public ApiResponse<Object> logout(@Valid @RequestBody LogoutRequest request) {
		authService.logout(request);
		return ApiResponse.success();
	}

	@PostMapping("/email-verifications/confirm")
	public ApiResponse<EmailVerificationResponse> confirmEmailVerification(
		@Valid @RequestBody EmailVerificationConfirmRequest request
	) {
		return ApiResponse.success(emailVerificationService.confirm(request));
	}

	@PostMapping("/email-verifications/resend")
	public ApiResponse<EmailVerificationResponse> resendEmailVerification(
		@Valid @RequestBody EmailVerificationResendRequest request
	) {
		return ApiResponse.success(emailVerificationService.resend(request));
	}
}
