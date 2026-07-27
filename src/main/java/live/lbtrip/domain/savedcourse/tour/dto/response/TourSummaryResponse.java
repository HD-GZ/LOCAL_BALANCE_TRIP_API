package live.lbtrip.domain.savedcourse.tour.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TourSummaryResponse(
    @Schema(description = "완주 여부(모든 장소 방문 시 true)", example = "true")
    boolean completed,

    @Schema(description = "방문한 장소 수", example = "5")
    int visitedPlaceCount,

    @Schema(description = "전체 장소 수", example = "5")
    int totalPlaceCount,

    @Schema(description = "소요 시간(분)", example = "130")
    long durationMinutes
) {

    public static TourSummaryResponse of(
        boolean completed, int visitedPlaceCount, int totalPlaceCount, long durationMinutes
    ) {
        return new TourSummaryResponse(completed, visitedPlaceCount, totalPlaceCount, durationMinutes);
    }
}
