package live.lbtrip.domain.incentive.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.domain.incentive.model.Incentive;
import live.lbtrip.domain.incentive.model.IncentiveRegion;

public record IncentiveResponse(
    @Schema(description = "인센티브 ID", example = "1")
    Long incentiveId,

    @Schema(description = "행사 제목", example = "KTX 인구감소지역 할인")
    String title,

    @Schema(description = "행사 페이지 URL", example = "https://www.letskorail.com/event/discount")
    String url,

    @Schema(description = "행사 부가 설명. 없으면 null.", nullable = true, example = "코레일 공식 채널로 이동")
    String description,

    @Schema(description = "적용 지역 목록(법정동 코드)")
    List<RegionResponse> regions
) {

    public record RegionResponse(
        @Schema(description = "법정동 시도 코드(2자리)", example = "46")
        String ldongRegnCd,

        @Schema(description = "법정동 시군구 코드(3자리)", example = "710")
        String ldongSignguCd
    ) {

        private static RegionResponse from(IncentiveRegion region) {
            return new RegionResponse(region.getLdongRegnCd(), region.getLdongSignguCd());
        }
    }

    public static IncentiveResponse from(Incentive incentive) {
        return new IncentiveResponse(
            incentive.getId(),
            incentive.getTitle(),
            incentive.getUrl(),
            incentive.getDescription(),
            incentive.getRegions().stream().map(RegionResponse::from).toList()
        );
    }
}
