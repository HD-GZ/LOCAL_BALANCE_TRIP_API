package live.lbtrip.domain.savedcourse.controller;

import static live.lbtrip.global.error.ErrorCode.COURSE_NOT_FOUND;
import static live.lbtrip.global.error.ErrorCode.DUPLICATE_SAVE_COURSE;
import static live.lbtrip.global.error.ErrorCode.INVALID_ACCESS_TOKEN;
import static live.lbtrip.global.error.ErrorCode.USER_NOT_FOUND;
import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "SavedCourse", description = "저장 코스 API")
public interface CourseSaveApi {

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
}
