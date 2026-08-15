package live.lbtrip.domain.auth.controller;

import static live.lbtrip.global.error.ErrorCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.domain.auth.dto.request.PasswordResetCodeRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetConfirmRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetRequest;
import live.lbtrip.domain.auth.dto.response.PasswordResetCodeResponse;
import live.lbtrip.domain.auth.dto.response.PasswordResetTokenResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;

@Tag(name = "PasswordReset", description = "비밀번호 찾기 API")
public interface PasswordResetApi {

    @Operation(
        summary = "비밀번호 재설정 인증 코드 요청",
        description = """
            가입된 이메일로 6자리 비밀번호 재설정 인증 코드를 발송합니다.
            인증 코드 만료까지 남은 시간(초)을 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "인증 코드 발송 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        USER_NOT_FOUND,
        USER_WITHDRAWN,
        EMAIL_NOT_VERIFIED,
        EMAIL_SEND_FAILED
    })
    ResponseEntity<PasswordResetCodeResponse> requestPasswordReset(
        @Valid @RequestBody PasswordResetCodeRequest request
    );

    @Operation(
        summary = "비밀번호 재설정 인증 코드 확인",
        description = """
            이메일로 발송된 6자리 인증 코드를 확인합니다.
            확인에 성공하면 새 비밀번호 설정에 사용할 일회용 리셋 토큰을 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "인증 코드 확인 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        USER_NOT_FOUND,
        USER_WITHDRAWN,
        EMAIL_NOT_VERIFIED,
        PASSWORD_RESET_CODE_NOT_FOUND,
        PASSWORD_RESET_CODE_EXPIRED,
        PASSWORD_RESET_CODE_USED
    })
    ResponseEntity<PasswordResetTokenResponse> confirmPasswordReset(
        @Valid @RequestBody PasswordResetConfirmRequest request
    );

    @Operation(
        summary = "새 비밀번호 설정",
        description = """
            리셋 토큰을 검증하고 새 비밀번호로 변경합니다.
            변경에 성공하면 해당 계정의 모든 리프레시 토큰이 폐기됩니다.
            """
    )
    @ApiSuccessResponse(description = "비밀번호 재설정 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        PASSWORD_RESET_TOKEN_NOT_FOUND,
        PASSWORD_RESET_TOKEN_EXPIRED,
        PASSWORD_RESET_TOKEN_USED,
        USER_WITHDRAWN
    })
    ResponseEntity<Void> resetPassword(
        @Valid @RequestBody PasswordResetRequest request
    );
}
