package live.lbtrip.domain.recommendation.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.recommendation.model.entity.SavedCourse;

public record SavedCourseListResponse(
    @Schema(description = "저장한 코스 총 개수", example = "12")
    long totalCount,

    @Schema(description = "현재 페이지 번호(1부터 시작)", example = "1")
    int page,

    @Schema(description = "페이지 크기", example = "4")
    int limit,

    @Schema(description = "전체 페이지 수", example = "3")
    int totalPages,

    @Schema(description = "저장 코스 목록(최근 저장 순)")
    List<InnerSavedCourseResponse> courses
) {

    public record InnerSavedCourseResponse(
        @Schema(description = "저장 코스 식별자", example = "1")
        Long savedCourseId,

        @Schema(description = "코스명", example = "전라남도 담양군 골목 미식 코스")
        String courseName,

        @Schema(description = "코스 대표 이미지 URL. 없으면 null.", nullable = true)
        String imageUrl
    ) {

        private static InnerSavedCourseResponse from(SavedCourse savedCourse) {
            return new InnerSavedCourseResponse(
                savedCourse.getId(),
                savedCourse.getCourseName(),
                savedCourse.getImageUrl()
            );
        }
    }

    public static SavedCourseListResponse from(Page<SavedCourse> savedCourses) {
        return new SavedCourseListResponse(
            savedCourses.getTotalElements(),
            savedCourses.getNumber() + 1,
            savedCourses.getSize(),
            savedCourses.getTotalPages(),
            savedCourses.getContent().stream().map(InnerSavedCourseResponse::from).toList()
        );
    }
}
