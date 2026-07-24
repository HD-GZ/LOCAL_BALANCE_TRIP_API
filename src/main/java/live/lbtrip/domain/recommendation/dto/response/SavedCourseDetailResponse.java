package live.lbtrip.domain.recommendation.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.incentive.model.Incentive;
import live.lbtrip.domain.recommendation.model.entity.SavedCourse;
import live.lbtrip.domain.recommendation.model.entity.SavedCoursePlace;

public record SavedCourseDetailResponse(
    @Schema(description = "저장 코스 식별자", example = "1")
    Long savedCourseId,

    @Schema(description = "지역 표시명(시/도 풀네임 + 시/군)", example = "전라남도 담양군")
    String regionName,

    @Schema(description = "코스명", example = "전라남도 담양군 골목 미식 코스")
    String title,

    @Schema(description = "코스 경유지 타임라인(방문 순서대로)")
    List<InnerPlaceResponse> places,

    @Schema(description = "이 코스에 적용 가능한 혜택 목록")
    List<InnerBenefitResponse> benefits
) {

    public record InnerPlaceResponse(
        @Schema(description = "방문 순서", example = "1")
        int order,

        @Schema(description = "장소명", example = "죽녹원")
        String name,

        @Schema(description = "장소 소개")
        String description,

        @Schema(description = "장소 이미지 URL")
        String imageUrl,

        @Schema(description = "경도(온디맨드 지도용)", example = "126.9816417636")
        Double longitude,

        @Schema(description = "위도(온디맨드 지도용)", example = "35.3244279032")
        Double latitude,

        @Schema(description = "이전 장소로부터 도보 이동 시간(분). 첫 장소는 null.", nullable = true, example = "6")
        Integer walkMinutes,

        @Schema(description = "오디오가이드 제공 여부", example = "true")
        boolean hasAudio,

        @Schema(description = "오디오가이드 재생 URL. 미지원 장소는 null.", nullable = true)
        String audioUrl
    ) {

        private static InnerPlaceResponse from(SavedCoursePlace place) {
            return new InnerPlaceResponse(
                place.getVisitOrder(),
                place.getName(),
                place.getOverview(),
                place.getImageUrl(),
                place.getLongitude(),
                place.getLatitude(),
                place.getWalkMinutes(),
                place.isHasAudio(),
                place.getAudioUrl()
            );
        }
    }

    public record InnerBenefitResponse(
        @Schema(description = "혜택명", example = "KTX 인구감소지역 할인")
        String title,

        @Schema(description = "혜택 부가 설명. 없으면 null.", nullable = true, example = "코레일 공식 채널로 이동")
        String description,

        @Schema(description = "혜택 페이지 URL", example = "https://www.letskorail.com/event/discount")
        String url
    ) {

        private static InnerBenefitResponse from(Incentive incentive) {
            return new InnerBenefitResponse(incentive.getTitle(), incentive.getDescription(), incentive.getUrl());
        }
    }

    public static SavedCourseDetailResponse of(SavedCourse savedCourse, List<Incentive> incentives) {
        return new SavedCourseDetailResponse(
            savedCourse.getId(),
            savedCourse.getRegionName(),
            savedCourse.getCourseName(),
            savedCourse.getPlaces().stream().map(InnerPlaceResponse::from).toList(),
            incentives.stream().map(InnerBenefitResponse::from).toList()
        );
    }
}
