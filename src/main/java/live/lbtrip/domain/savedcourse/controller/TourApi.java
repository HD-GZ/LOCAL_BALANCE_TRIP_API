package live.lbtrip.domain.savedcourse.controller;

import static live.lbtrip.global.error.ErrorCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import live.lbtrip.domain.savedcourse.dto.response.TourProgressResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourSummaryResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "Tour", description = "GPS 슬로우 투어 API")
public interface TourApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "투어 시작",
        description = """
            저장 코스의 GPS 슬로우 투어를 시작하고 여행 상태를 TRAVELING으로 전환합니다.
            중도 종료한 투어를 다시 시작하면 기존 방문 기록을 유지한 채 이어서 진행합니다.
            이미 완주한 코스는 409 Conflict 응답을 반환합니다.
            응답으로 장소별 방문 현황을 반환하므로 재시작 시 진행 상황을 복원할 수 있습니다.
            """
    )
    @ApiSuccessResponse(description = "투어 시작 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND,
        TOUR_ALREADY_COMPLETED
    })
    ResponseEntity<TourProgressResponse> startTour(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "장소 방문 체크인",
        description = """
            투어 진행 중인 코스의 장소에 방문 체크인합니다.
            이미 체크인한 장소에 다시 요청해도 성공 응답을 반환합니다(최초 방문 시각 유지).
            투어 진행 중(TRAVELING)이 아니면 409 Conflict 응답을 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "체크인 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND,
        SAVED_COURSE_PLACE_NOT_FOUND,
        TOUR_NOT_IN_PROGRESS
    })
    ResponseEntity<Void> checkIn(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId,
        @Parameter(description = "장소 식별자", example = "1") @PathVariable Long placeId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "투어 종료",
        description = """
            진행 중인 투어를 종료하고 요약(완주 여부, 방문 장소 수, 소요 시간)을 반환합니다.
            모든 장소를 방문한 경우 여행 상태가 COMPLETED(완주)로 전환됩니다.
            일부만 방문한 중도 종료는 TRAVELING 상태를 유지해 나중에 이어서 진행할 수 있습니다.
            투어 진행 중(TRAVELING)이 아니면 409 Conflict 응답을 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "투어 종료 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND,
        TOUR_NOT_IN_PROGRESS
    })
    ResponseEntity<TourSummaryResponse> endTour(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId
    );
}
