package live.lbtrip.admin.incentive.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.domain.incentive.model.Incentive;
import live.lbtrip.domain.incentive.model.IncentiveRegion;

public record AdminIncentiveResponse(
    @Schema(description = "인센티브 ID", example = "1")
    Long incentiveId,

    @Schema(description = "행사 제목", example = "KTX 인구감소지역 할인")
    String title,

    @Schema(description = "영문 행사 제목. 없으면 null.", nullable = true, example = "KTX Discount for Depopulation Areas")
    String titleEn,

    @Schema(description = "행사 페이지 URL", example = "https://www.letskorail.com/event/discount")
    String url,

    @Schema(description = "행사 부가 설명. 없으면 null.", nullable = true, example = "코레일 공식 채널로 이동")
    String description,

    @Schema(description = "영문 행사 부가 설명. 없으면 null.", nullable = true, example = "Opens the official Korail channel")
    String descriptionEn,

    @Schema(description = "혜택 시작일. 레거시 데이터는 일시적으로 null일 수 있습니다.", nullable = true, example = "2026-07-01")
    LocalDate startDate,

    @Schema(description = "혜택 종료일. null이면 종료일 없이 유지됩니다.", nullable = true, example = "2026-08-31")
    LocalDate endDate,

    @Schema(description = "적용 지역 목록")
    List<RegionResponse> regions
) {

    public record RegionResponse(
        @Schema(description = "지역 후보 ID", example = "1")
        Long regionCandidateId,

        @Schema(description = "지역 이름", example = "전라남도 담양군")
        String name
    ) {

        private static RegionResponse from(IncentiveRegion region) {
            return new RegionResponse(
                region.getRegionCandidate().getId(),
                region.getRegionCandidate().getName());
        }
    }

    public static AdminIncentiveResponse from(Incentive incentive) {
        return new AdminIncentiveResponse(
            incentive.getId(),
            incentive.getTitle(),
            incentive.getTitleEn(),
            incentive.getUrl(),
            incentive.getDescription(),
            incentive.getDescriptionEn(),
            incentive.getStartDate(),
            incentive.getEndDate(),
            incentive.getRegions().stream().map(RegionResponse::from).toList()
        );
    }
}
