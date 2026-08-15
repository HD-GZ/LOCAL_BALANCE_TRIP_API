package live.lbtrip.domain.home.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;

public record PopularCourseListResponse(
    @Schema(description = "인기 지역 대표 코스 목록")
    List<InnerPopularCourse> courses
) {

    public record InnerPopularCourse(
        @Schema(description = "코스 식별자", example = "10")
        Long courseId,

        @Schema(description = "코스명", example = "담양 골목 미식 코스")
        String title,

        @Schema(description = "추천 이유")
        String reason,

        @Schema(description = "대표 이미지 URL")
        String imageUrl,

        @Schema(description = "지역명", example = "전라남도 담양군")
        String regionName
    ) {

        public static InnerPopularCourse from(GeneratedCourse course) {
            return new InnerPopularCourse(
                course.getId(),
                course.getName(),
                course.getReason(),
                course.getImageUrl(),
                course.getRecommendedRegion().getRegionName());
        }
    }

    public static PopularCourseListResponse of(List<GeneratedCourse> courses) {
        return new PopularCourseListResponse(courses.stream().map(InnerPopularCourse::from).toList());
    }
}
