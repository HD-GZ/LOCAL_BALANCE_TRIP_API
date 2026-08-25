package live.lbtrip.domain.home.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.tourism.model.vo.LocalizedTourEvent;

public record HomeEventResponse(
    @Schema(description = "추천/인기 지역별 진행 중·예정 행사 탭 목록")
    List<InnerRegionTab> regions
) {

    public record InnerRegionTab(
        @Schema(description = "지역명", example = "전라남도 담양군")
        String regionName,

        @Schema(description = "지역 후보 ID", example = "1")
        Long regionCandidateId,

        @Schema(description = "해당 지역의 진행 중·예정 행사")
        List<InnerEvent> events
    ) {
    }

    public record InnerEvent(
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

        public static InnerEvent from(LocalizedTourEvent event) {
            return new InnerEvent(event.title(), event.imageUrl(), event.startDate(), event.endDate(), event.address());
        }
    }

    public static InnerRegionTab tab(String regionName, Long regionCandidateId, List<LocalizedTourEvent> events) {
        return new InnerRegionTab(regionName, regionCandidateId, events.stream().map(InnerEvent::from).toList());
    }

    public static HomeEventResponse of(List<InnerRegionTab> regions) {
        return new HomeEventResponse(regions);
    }
}
