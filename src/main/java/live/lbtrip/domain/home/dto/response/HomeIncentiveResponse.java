package live.lbtrip.domain.home.dto.response;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.incentive.model.vo.LocalizedIncentive;

public record HomeIncentiveResponse(
    @Schema(description = "추천/인기 지역별 진행중 인센티브 탭 목록")
    List<InnerRegionTab> regions
) {

    public record InnerRegionTab(
        @Schema(description = "지역명", example = "충청북도 제천시")
        String regionName,

        @Schema(description = "지역 후보 ID", example = "1")
        Long regionCandidateId,

        @Schema(description = "해당 지역의 진행중 인센티브")
        List<InnerIncentive> incentives
    ) {
    }

    public record InnerIncentive(
        @Schema(description = "인센티브 제목", example = "제천 체류형 관광 지원")
        String title,

        @Schema(description = "부가 설명", nullable = true)
        String description,

        @Schema(description = "행사 페이지 URL")
        String url,

        @Schema(description = "종료일. 상시(마감 없음)면 null.", nullable = true, example = "2026-08-31")
        LocalDate endDate,

        @Schema(description = "마감까지 남은 일수. 상시면 null.", nullable = true, example = "12")
        Long dday
    ) {

        public static InnerIncentive of(LocalizedIncentive incentive, LocalDate today) {
            Long dday = incentive.endDate() == null
                ? null
                : ChronoUnit.DAYS.between(today, incentive.endDate());
            return new InnerIncentive(
                incentive.title(),
                incentive.description(),
                incentive.url(),
                incentive.endDate(),
                dday);
        }
    }

    public static InnerRegionTab tab(String regionName, Long regionCandidateId,
        List<LocalizedIncentive> incentives, LocalDate today) {
        return new InnerRegionTab(regionName, regionCandidateId,
            incentives.stream().map(i -> InnerIncentive.of(i, today)).toList());
    }

    public static HomeIncentiveResponse of(List<InnerRegionTab> regions) {
        return new HomeIncentiveResponse(regions);
    }
}
