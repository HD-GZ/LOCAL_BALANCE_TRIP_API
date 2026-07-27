package live.lbtrip.domain.savedcourse.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.SavedCoursePlace;

public record TourProgressResponse(
    @Schema(description = "저장 코스 식별자", example = "1")
    Long savedCourseId,

    @Schema(description = "여행 상태(BEFORE_TRIP: 여행전, TRAVELING: 여행중, COMPLETED: 완주)", example = "TRAVELING")
    SavedCourseStatus status,

    @Schema(description = "투어 시작 시각", example = "2026-07-27T10:30:00")
    LocalDateTime tourStartedAt,

    @Schema(description = "장소별 방문 현황(방문 순서대로)")
    List<InnerTourPlaceResponse> places
) {

    public record InnerTourPlaceResponse(
        @Schema(description = "장소 식별자", example = "1")
        Long placeId,

        @Schema(description = "방문 순서", example = "1")
        int order,

        @Schema(description = "장소명", example = "죽녹원")
        String name,

        @Schema(description = "방문 완료 여부", example = "false")
        boolean visited,

        @Schema(description = "방문 시각. 미방문 장소는 null.", nullable = true, example = "2026-07-27T10:45:00")
        LocalDateTime visitedAt
    ) {

        private static InnerTourPlaceResponse from(SavedCoursePlace place) {
            return new InnerTourPlaceResponse(
                place.getId(),
                place.getVisitOrder(),
                place.getName(),
                place.isVisited(),
                place.getVisitedAt()
            );
        }
    }

    public static TourProgressResponse from(SavedCourse savedCourse) {
        return new TourProgressResponse(
            savedCourse.getId(),
            savedCourse.getStatus(),
            savedCourse.getTourStartedAt(),
            savedCourse.getPlaces().stream().map(InnerTourPlaceResponse::from).toList()
        );
    }
}
