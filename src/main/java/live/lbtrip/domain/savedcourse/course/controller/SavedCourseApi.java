package live.lbtrip.domain.savedcourse.course.controller;

import static live.lbtrip.global.error.ErrorCode.*;
import static org.springframework.http.HttpStatus.CREATED;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.domain.savedcourse.course.dto.request.SavedCourseStatusUpdateRequest;
import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseDetailResponse;
import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseListResponse;
import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.PageQueryRequest;
import live.lbtrip.global.web.UserId;

@Tag(name = "SavedCourse", description = "저장 코스 API")
public interface SavedCourseApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "코스 저장",
        description = """
            추천 코스를 사용자의 저장 목록(마이 > SAVE)에 스냅샷으로 복사합니다.
            이미 저장된 코스는 409 Conflict 응답을 반환합니다.
            """
    )
    @ApiSuccessResponse(status = CREATED, description = "코스 저장 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        USER_NOT_FOUND,
        COURSE_NOT_FOUND,
        DUPLICATE_SAVE_COURSE
    })
    ResponseEntity<Void> saveCourse(
        @UserId Long userId,
        @Parameter(description = "코스 식별자(서비스 생성 코스 ID)", example = "1")
        @PathVariable Long courseId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "저장 코스 목록 조회", description = "저장한 코스를 최근 저장 순으로 페이지네이션하여 조회합니다. limit 생략 시 10개씩 조회합니다. status 지정 시 해당 여행 상태의 코스만 조회합니다.")
    @ApiSuccessResponse(description = "조회 성공")
    @ApiErrorCodeResponses({INVALID_INPUT_VALUE, INVALID_ACCESS_TOKEN})
    ResponseEntity<SavedCourseListResponse> getSavedCourses(
        @UserId Long userId,
        @Parameter(description = "여행 상태 필터(BEFORE_TRIP: 여행전, TRAVELING: 여행중, COMPLETED: 완주). 생략 시 전체 조회.", example = "TRAVELING")
        @RequestParam(required = false) SavedCourseStatus status,
        @Valid @ParameterObject @ModelAttribute PageQueryRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "저장 코스 상세 조회", description = "저장한 코스의 타임라인과 적용 가능한 혜택을 조회합니다.")
    @ApiSuccessResponse(description = "조회 성공")
    @ApiErrorCodeResponses({INVALID_ACCESS_TOKEN, SAVED_COURSE_NOT_FOUND})
    ResponseEntity<SavedCourseDetailResponse> getSavedCourseDetail(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "[테스트] 저장 코스 여행 상태 변경",
        description = """
            클라이언트 테스트용 API입니다.
            저장 코스의 여행 상태(BEFORE_TRIP: 여행전, TRAVELING: 여행중, COMPLETED: 완주)를 변경합니다.
            본인이 저장한 코스만 변경할 수 있습니다.
            """
    )
    @ApiSuccessResponse(description = "상태 변경 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND
    })
    ResponseEntity<Void> updateSavedCourseStatus(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId,
        @Valid @RequestBody SavedCourseStatusUpdateRequest request
    );
}
