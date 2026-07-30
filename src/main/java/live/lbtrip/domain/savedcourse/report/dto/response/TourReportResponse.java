package live.lbtrip.domain.savedcourse.report.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;

public record TourReportResponse(
    @Schema(description = "코스명(공유 카드 제목)", example = "공주 원도심 슬로우 투어")
    String courseName,

    @Schema(description = "코스 썸네일 이미지 URL(공유 카드 이미지, 없으면 null)", example = "https://images.example.com/course.jpg")
    String imageUrl,

    @Schema(description = "방문한 장소 수", example = "5")
    int visitedPlaceCount,

    @Schema(description = "투어 소요 시간(분)", example = "130")
    long durationMinutes,

    @Schema(description = "지역 소비 금액(등록된 환급 증빙 금액 합계, 원)", example = "52000")
    int totalSpentAmount,

    @Schema(description = "투어 종료 시각(공유 카드 날짜)", example = "2026-07-17T15:30:00")
    LocalDateTime tourEndedAt
) {

    public static TourReportResponse from(SavedCourse savedCourse) {
        return new TourReportResponse(
            savedCourse.getCourseName(),
            savedCourse.getImageUrl(),
            savedCourse.countVisitedPlaces(),
            savedCourse.tourDurationMinutes(),
            savedCourse.calculateTotalReceiptAmount(),
            savedCourse.getTourEndedAt()
        );
    }
}
