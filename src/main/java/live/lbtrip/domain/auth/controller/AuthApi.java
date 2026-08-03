package live.lbtrip.domain.auth.controller;

import static live.lbtrip.global.error.ErrorCode.*;
import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "Auth", description = "회원가입, 로그인, 토큰, 이메일 인증 API")
public interface AuthApi {

    @Operation(
        summary = "회원가입",
        description = """
            회원가입 후 6자리 이메일 인증 코드를 발송합니다.
            가입된 사용자 정보와 인증 코드 만료까지 남은 시간(초)을 반환합니다.
            요청 body의 gender는 MALE, FEMALE, NOT_SPECIFIED 중 하나입니다.
            """
    )
    @ApiSuccessResponse(status = CREATED, description = "회원가입 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        PASSWORD_CONFIRM_MISMATCH,
        REQUIRED_AGREEMENT_NOT_ACCEPTED,
        DUPLICATE_EMAIL,
        EMAIL_SEND_FAILED
    })
    ResponseEntity<SignupResponse> signup(
        @Valid @RequestBody SignupRequest request
    );

    @Operation(
        summary = "로그인",
        description = """
            이메일과 비밀번호로 로그인을 처리합니다.
            access token과 refresh token을 반환합니다.
            이메일 인증을 완료하지 않은 사용자는 로그인할 수 없습니다.
            탈퇴 후 30일 이내에 로그인하면 탈퇴가 철회됩니다.
            """
    )
    @ApiSuccessResponse(description = "로그인 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        INVALID_LOGIN_CREDENTIALS,
        EMAIL_NOT_VERIFIED,
        USER_WITHDRAWN
    })
    ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    );

    @Operation(
        summary = "토큰 갱신",
        description = """
            refresh token을 검증하고 새 access token을 발급합니다.
            새 access token과 요청에 사용한 기존 refresh token을 함께 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "토큰 갱신 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        INVALID_REFRESH_TOKEN,
        EXPIRED_REFRESH_TOKEN
    })
    ResponseEntity<TokenResponse> refreshToken(
        @Valid @RequestBody TokenRefreshRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "로그아웃",
        description = """
            로그인한 사용자의 refresh token을 삭제해 로그아웃 처리합니다.
            """
    )
    @ApiSuccessResponse(description = "로그아웃 성공")
    @ApiErrorCodeResponses(INVALID_ACCESS_TOKEN)
    ResponseEntity<Void> logout(@UserId Long userId);

    @Operation(
        summary = "이메일 인증 확인",
        description = """
            6자리 이메일 인증 코드를 확인합니다.
            인증에 성공하면 사용자 상태를 ACTIVE로 변경해 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "이메일 인증 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        EMAIL_VERIFICATION_CODE_EXPIRED,
        EMAIL_VERIFICATION_CODE_USED,
        EMAIL_VERIFICATION_CODE_NOT_FOUND,
        USER_WITHDRAWN
    })
    ResponseEntity<EmailVerificationResponse> confirmEmailVerification(
        @Valid @RequestBody EmailVerificationConfirmRequest request
    );

    @Operation(
        summary = "이메일 인증 코드 재발송",
        description = """
            이메일 인증을 완료하지 않은 사용자에게 6자리 인증 코드를 다시 발송합니다.
            사용자 정보와 현재 인증 대기 상태를 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "인증 코드 재발송 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        USER_NOT_FOUND,
        USER_WITHDRAWN,
        EMAIL_ALREADY_VERIFIED,
        EMAIL_SEND_FAILED
    })
    ResponseEntity<EmailVerificationResponse> resendEmailVerification(
        @Valid @RequestBody EmailVerificationResendRequest request
    );
}
