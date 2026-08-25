package live.lbtrip.domain.recommendation.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.incentive.model.vo.LocalizedIncentive;
import live.lbtrip.domain.recommendation.model.entity.CoursePlace;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.tourism.model.vo.LocalizedTourEvent;

public record CourseDetailResponse(
    @Schema(description = "코스 식별자(서비스 생성 코스 ID)", example = "1")
    Long courseId,

    @Schema(description = "지역 표시명(시/도 풀네임 + 시/군)", example = "전라남도 담양군")
    String regionName,

    @Schema(description = "코스명", example = "전라남도 담양군 골목 미식 코스")
    String title,

    @Schema(description = "코스 경유지 타임라인(방문 순서대로)")
    List<InnerPlaceResponse> places,

    @Schema(description = "이 코스에 적용 가능한 혜택 목록")
    List<InnerBenefitResponse> benefits,

    @Schema(description = "이 코스 지역에서 진행 중이거나 예정된 행사·축제 목록")
    List<InnerEventResponse> events
) {

    public record InnerEventResponse(
        @Schema(description = "행사명", example = "담양 대나무축제")
        String title,

        @Schema(description = "대표 이미지 URL", nullable = true)
        String imageUrl,

        @Schema(description = "행사 시작일", example = "2026-09-01")
        LocalDate startDate,

        @Schema(description = "행사 종료일", example = "2026-09-05")
        LocalDate endDate,

        @Schema(description = "행사 장소 주소", nullable = true, example = "전라남도 담양군 죽녹원로 119")
        String address
    ) {

        private static InnerEventResponse from(LocalizedTourEvent event) {
            return new InnerEventResponse(
                event.title(), event.imageUrl(), event.startDate(), event.endDate(), event.address());
        }
    }

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

        private static InnerPlaceResponse from(CoursePlace place) {
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

    public static CourseDetailResponse of(
        GeneratedCourse course, List<LocalizedIncentive> incentives, List<LocalizedTourEvent> events
    ) {
        return new CourseDetailResponse(
            course.getId(),
            course.getRecommendedRegion().getRegionName(),
            course.getName(),
            course.getPlaces().stream().map(InnerPlaceResponse::from).toList(),
            incentives.stream().map(InnerBenefitResponse::from).toList(),
            events.stream().map(InnerEventResponse::from).toList()
        );
    }
}
