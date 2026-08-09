package live.lbtrip.domain.home.dto.response;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.incentive.model.Incentive;

public record HomeIncentiveResponse(
    @Schema(description = "추천/인기 지역별 진행중 인센티브 탭 목록")
    List<InnerRegionTab> regions
) {

    public record InnerRegionTab(
        @Schema(description = "지역명", example = "충청북도 제천시")
        String regionName,

        @Schema(description = "법정동 시도 코드", example = "43")
        String ldongRegnCd,

        @Schema(description = "법정동 시군구 코드", example = "150")
        String ldongSignguCd,

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

        public static InnerIncentive of(Incentive incentive, LocalDate today) {
            Long dday = incentive.getEndDate() == null
                ? null
                : ChronoUnit.DAYS.between(today, incentive.getEndDate());
            return new InnerIncentive(
                incentive.getTitle(),
                incentive.getDescription(),
                incentive.getUrl(),
                incentive.getEndDate(),
                dday);
        }
    }

    public static InnerRegionTab tab(String regionName, String regn, String signgu, List<Incentive> incentives, LocalDate today) {
        return new InnerRegionTab(regionName, regn, signgu,
            incentives.stream().map(i -> InnerIncentive.of(i, today)).toList());
    }

    public static HomeIncentiveResponse of(List<InnerRegionTab> regions) {
        return new HomeIncentiveResponse(regions);
    }
}
