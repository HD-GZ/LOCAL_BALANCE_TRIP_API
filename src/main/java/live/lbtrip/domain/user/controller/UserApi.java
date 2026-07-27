package live.lbtrip.domain.user.controller;

import static live.lbtrip.global.error.ErrorCode.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.domain.user.dto.request.EmailAvailabilityRequest;
import live.lbtrip.domain.user.dto.request.UserUpdateRequest;
import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;
import live.lbtrip.domain.user.dto.response.UserResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "User", description = "사용자 API")
public interface UserApi {

    @Operation(summary = "이메일 사용 가능 여부 확인", description = "가입 전 이메일 중복 여부를 확인합니다.")
    @ApiSuccessResponse(description = "조회 성공")
    @ApiErrorCodeResponses(INVALID_INPUT_VALUE)
    ResponseEntity<EmailAvailabilityResponse> checkEmailAvailability(
        @Valid @ParameterObject @ModelAttribute EmailAvailabilityRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiSuccessResponse(description = "조회 성공")
    @ApiErrorCodeResponses({INVALID_ACCESS_TOKEN, USER_NOT_FOUND})
    ResponseEntity<UserResponse> getUser(@UserId Long userId);

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "내 정보 수정",
        description = "이름, 생년월일, 성별, 비밀번호를 부분 수정합니다. 값을 보낸 필드만 변경되며, 비밀번호 변경 시 현재 비밀번호 검증이 필요합니다. 이메일은 변경할 수 없습니다."
    )
    @ApiSuccessResponse(description = "수정 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        INVALID_ACCESS_TOKEN,
        USER_NOT_FOUND,
        CURRENT_PASSWORD_REQUIRED,
        CURRENT_PASSWORD_MISMATCH,
        PASSWORD_CONFIRM_MISMATCH
    })
    ResponseEntity<UserResponse> updateUser(@UserId Long userId, @Valid @RequestBody UserUpdateRequest request);
}
