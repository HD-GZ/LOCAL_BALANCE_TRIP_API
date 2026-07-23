package live.lbtrip.domain.recommendation.controller;

import static live.lbtrip.global.error.ErrorCode.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.domain.recommendation.dto.request.SavedCourseListRequest;
import live.lbtrip.domain.recommendation.dto.response.SavedCourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.SavedCourseListResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "SavedCourse", description = "저장 코스 API")
public interface SavedCourseApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "저장 코스 목록 조회", description = "저장한 코스를 최근 저장 순으로 페이지네이션하여 조회합니다.")
    @ApiSuccessResponse(description = "조회 성공")
    @ApiErrorCodeResponses({INVALID_INPUT_VALUE, INVALID_ACCESS_TOKEN})
    ResponseEntity<SavedCourseListResponse> getSavedCourses(
        @UserId Long userId,
        @Valid @ParameterObject @ModelAttribute SavedCourseListRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "저장 코스 상세 조회", description = "저장한 코스의 타임라인과 적용 가능한 혜택을 조회합니다.")
    @ApiSuccessResponse(description = "조회 성공")
    @ApiErrorCodeResponses({INVALID_ACCESS_TOKEN, SAVED_COURSE_NOT_FOUND})
    ResponseEntity<SavedCourseDetailResponse> getSavedCourseDetail(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId
    );
}
