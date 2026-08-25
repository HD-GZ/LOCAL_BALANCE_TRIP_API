package live.lbtrip.domain.savedcourse.share.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.domain.incentive.model.vo.LocalizedIncentive;
import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseDetailResponse;
import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseDetailResponse.InnerBenefitResponse;
import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseDetailResponse.InnerPlaceResponse;
import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseDetailResponse.InnerTrailResponse;
import live.lbtrip.domain.tourism.model.entity.TrailCourse;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;

public record SharedCourseDetailResponse(
    @Schema(description = "저장 코스 식별자", example = "1")
    Long savedCourseId,

    @Schema(description = "코스를 공유한 사용자 이름", example = "김민서")
    String sharedByName,

    @Schema(description = "코스 대표 이미지 URL", nullable = true, example = "https://images.example.com/course.jpg")
    String imageUrl,

    @Schema(description = "지역 표시명(시/도 풀네임 + 시/군)", example = "전라남도 담양군")
    String regionName,

    @Schema(description = "코스명", example = "전라남도 담양군 골목 미식 코스")
    String title,

    @Schema(description = "여행 상태(BEFORE_TRIP: 여행전, TRAVELING: 여행중, COMPLETED: 완주)", example = "BEFORE_TRIP")
    SavedCourseStatus status,

    @Schema(description = "코스 경유지 타임라인(방문 순서대로)")
    List<InnerPlaceResponse> places,

    @Schema(description = "이 코스에 적용 가능한 혜택 목록")
    List<InnerBenefitResponse> benefits,

    @Schema(description = "코스 지역의 근처 둘레길(두루누비) 목록. 거리 오름차순 최대 5개")
    List<InnerTrailResponse> trails
) {

    public static SharedCourseDetailResponse of(
        SavedCourse savedCourse, List<LocalizedIncentive> incentives, List<TrailCourse> trails
    ) {
        SavedCourseDetailResponse courseDetail = SavedCourseDetailResponse.of(savedCourse, incentives, trails);

        return new SharedCourseDetailResponse(
            courseDetail.savedCourseId(),
            savedCourse.getUser().getName(),
            savedCourse.getImageUrl(),
            courseDetail.regionName(),
            courseDetail.title(),
            courseDetail.status(),
            courseDetail.places(),
            courseDetail.benefits(),
            courseDetail.trails()
        );
    }
}
