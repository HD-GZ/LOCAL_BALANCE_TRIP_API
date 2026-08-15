package live.lbtrip.domain.savedcourse.share.controller;

import static live.lbtrip.global.error.ErrorCode.*;
import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import live.lbtrip.domain.savedcourse.share.dto.response.SharedCourseDetailResponse;
import live.lbtrip.domain.savedcourse.share.dto.response.ShareTokenResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "CourseShare", description = "저장 코스 공유 API")
public interface CourseShareApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "공유 토큰 발급",
        description = """
            저장 코스를 외부에 공유하기 위한 임시 토큰을 발급합니다.
            호출할 때마다 새 토큰이 발급되며, 각 토큰은 발급 시점부터 7일간 유효합니다.
            이전에 발급한 토큰은 각자의 만료 시각까지 계속 유효합니다.
            공유 URL 조립은 클라이언트 책임입니다.
            """
    )
    @ApiSuccessResponse(status = CREATED, description = "공유 토큰 발급 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND
    })
    ResponseEntity<ShareTokenResponse> issueShareToken(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId
    );

    @Operation(
        summary = "공유 코스 상세 조회",
        description = """
            공유 토큰으로 저장 코스의 상세 정보를 조회합니다.
            인증 없이 호출할 수 있으며, 공유한 사용자 이름과 코스 대표 이미지를 포함합니다.
            토큰이 없으면 404, 만료된 토큰이면 410을 응답합니다.
            """
    )
    @ApiSuccessResponse(description = "공유 코스 상세 조회 성공")
    @ApiErrorCodeResponses({
        SHARE_TOKEN_NOT_FOUND,
        SHARE_TOKEN_EXPIRED
    })
    ResponseEntity<SharedCourseDetailResponse> getSharedCourseDetail(
        @Parameter(description = "공유 토큰", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String token
    );
}
