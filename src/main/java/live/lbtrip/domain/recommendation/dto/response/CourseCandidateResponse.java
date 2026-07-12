package live.lbtrip.domain.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;

public record CourseCandidateResponse(
    @Schema(description = "코스 식별자(서비스 생성 코스 ID)", example = "1")
    Long courseId,

    @Schema(description = "코스명", example = "전라남도 담양군 골목 미식 코스")
    String title,

    @Schema(description = "대표 이미지 URL(첫 장소 이미지)")
    String imageUrl,

    @Schema(description = "추천 이유", example = "노포와 골목 상권 위주로 묶은 로컬 미식 동선이에요")
    String reason
) {

    public static CourseCandidateResponse from(GeneratedCourse course) {
        return new CourseCandidateResponse(
            course.getId(),
            course.getName(),
            course.getImageUrl(),
            course.getReason()
        );
    }
}
