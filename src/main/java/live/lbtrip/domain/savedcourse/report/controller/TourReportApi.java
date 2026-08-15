package live.lbtrip.domain.savedcourse.report.controller;

import static live.lbtrip.global.error.ErrorCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import live.lbtrip.domain.savedcourse.report.dto.response.TourReportResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "TourReport", description = "투어 리포트 API")
public interface TourReportApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "투어 리포트 조회",
        description = """
            투어를 종료한 저장 코스의 리포트를 조회합니다.
            리포트 지표(방문 장소 수, 소요 시간, 지역 소비 금액)와
            SNS 공유 카드에 필요한 데이터(코스명, 썸네일, 투어 종료 시각)를 반환합니다.
            투어를 종료하지 않은 코스는 409 Conflict 응답을 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "투어 리포트 조회 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND,
        TOUR_REPORT_NOT_AVAILABLE
    })
    ResponseEntity<TourReportResponse> getReport(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId
    );
}
