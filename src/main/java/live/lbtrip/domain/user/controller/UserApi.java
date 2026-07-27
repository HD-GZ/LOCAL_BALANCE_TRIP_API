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

    @Operation(
        summary = "이메일 사용 가능 여부 확인",
        description = """
            가입 전 이메일의 중복 여부를 확인합니다.
            이메일 사용 가능 여부를 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "이메일 사용 가능 여부 확인 성공")
    @ApiErrorCodeResponses(INVALID_INPUT_VALUE)
    ResponseEntity<EmailAvailabilityResponse> checkEmailAvailability(
        @Valid @ParameterObject @ModelAttribute EmailAvailabilityRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "내 정보 조회",
        description = """
            현재 로그인한 사용자의 프로필 정보를 조회합니다.
            """
    )
    @ApiSuccessResponse(description = "내 정보 조회 성공")
    @ApiErrorCodeResponses({INVALID_ACCESS_TOKEN, USER_NOT_FOUND})
    ResponseEntity<UserResponse> getUser(@UserId Long userId);

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "내 정보 수정",
        description = """
            현재 로그인한 사용자의 이름, 생년월일과 성별을 수정합니다.
            비밀번호는 요청 값이 있을 때만 변경하며, 이메일은 변경할 수 없습니다.
            수정된 프로필 정보를 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "내 정보 수정 성공")
    @ApiErrorCodeResponses({INVALID_INPUT_VALUE, INVALID_ACCESS_TOKEN, USER_NOT_FOUND})
    ResponseEntity<UserResponse> updateUser(@UserId Long userId, @Valid @RequestBody UserUpdateRequest request);
}
