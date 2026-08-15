package live.lbtrip.admin.auth.controller;

import static live.lbtrip.global.error.ErrorCode.INVALID_INPUT_VALUE;
import static live.lbtrip.global.error.ErrorCode.INVALID_LOGIN_CREDENTIALS;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.admin.auth.dto.request.AdminLoginRequest;
import live.lbtrip.admin.auth.dto.response.AdminTokenResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;

@Tag(name = "Admin Auth", description = "어드민 로그인 API")
public interface AdminAuthApi {

    @Operation(
        summary = "어드민 로그인",
        description = """
            이메일과 비밀번호로 어드민 로그인을 처리합니다.
            API 인증에 사용할 access token을 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "로그인 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        INVALID_LOGIN_CREDENTIALS
    })
    ResponseEntity<AdminTokenResponse> login(
        @Valid @RequestBody AdminLoginRequest request
    );
}
