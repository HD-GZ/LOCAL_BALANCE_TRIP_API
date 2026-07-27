package live.lbtrip.domain.savedcourse.dto.response;

import java.time.Duration;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.SavedCoursePlace;

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

    public static TourSummaryResponse of(SavedCourse savedCourse, boolean completed) {
        int visitedPlaceCount = (int) savedCourse.getPlaces().stream()
            .filter(SavedCoursePlace::isVisited)
            .count();
        long durationMinutes = savedCourse.getTourStartedAt() == null ? 0
            : Duration.between(savedCourse.getTourStartedAt(), savedCourse.getTourEndedAt()).toMinutes();

        return new TourSummaryResponse(
            completed,
            visitedPlaceCount,
            savedCourse.getPlaces().size(),
            durationMinutes
        );
    }
}
