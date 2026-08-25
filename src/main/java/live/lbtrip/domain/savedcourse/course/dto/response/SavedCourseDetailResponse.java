package live.lbtrip.domain.savedcourse.course.dto.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.incentive.model.vo.LocalizedIncentive;
import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.SavedCoursePlace;
import live.lbtrip.domain.tourism.model.entity.TrailCourse;

public record SavedCourseDetailResponse(
    @Schema(description = "저장 코스 식별자", example = "1")
    Long savedCourseId,

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

        private static InnerBenefitResponse from(LocalizedIncentive incentive) {
            return new InnerBenefitResponse(incentive.title(), incentive.description(), incentive.url());
        }
    }


    public record InnerTrailResponse(
        @Schema(description = "둘레길(두루누비) 코스명", example = "담양 메타세쿼이아길")
        String name,

        @Schema(description = "코스 거리(km). 없으면 null.", nullable = true, example = "12.50")
        BigDecimal distanceKm,

        @Schema(description = "소요 시간(분). 없으면 null.", nullable = true, example = "240")
        Integer requiredMinutes,

        @Schema(description = "난이도(1: 쉬움 ~ 3: 어려움). 없으면 null.", nullable = true, example = "2")
        Integer level
    ) {

        private static InnerTrailResponse from(TrailCourse trail) {
            return new InnerTrailResponse(
                trail.getName(), trail.getDistanceKm(), trail.getRequiredMinutes(), trail.getLevel());
        }
    }

    public static SavedCourseDetailResponse of(
        SavedCourse savedCourse, List<LocalizedIncentive> incentives, List<TrailCourse> trails
    ) {
        return new SavedCourseDetailResponse(
            savedCourse.getId(),
            savedCourse.getRegionName(),
            savedCourse.getCourseName(),
            savedCourse.getStatus(),
            savedCourse.getPlaces().stream().map(InnerPlaceResponse::from).toList(),
            incentives.stream().map(InnerBenefitResponse::from).toList(),
            trails.stream().map(InnerTrailResponse::from).toList()
        );
    }
}
