package live.lbtrip.domain.user.controller;

import static live.lbtrip.global.error.ErrorCode.INVALID_INPUT_VALUE;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;

@Tag(name = "User", description = "사용자 API")
public interface UserApi {

    @Operation(summary = "이메일 사용 가능 여부 확인", description = "가입 전 이메일 중복 여부를 확인합니다.")
    @ApiSuccessResponse(description = "조회 성공")
    @ApiErrorCodeResponses(INVALID_INPUT_VALUE)
    ResponseEntity<EmailAvailabilityResponse> checkEmailAvailability(
        @Parameter(description = "중복 확인할 이메일", example = "user@example.com", required = true)
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @RequestParam String email
    );
}
