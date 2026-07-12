package live.lbtrip.domain.admin.incentive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.domain.admin.incentive.model.Incentive;

public record IncentiveResponse(
    @Schema(description = "인센티브 ID", example = "1")
    Long incentiveId,

    @Schema(description = "행사 제목", example = "KTX 인구감소지역 할인")
    String title,

    @Schema(description = "행사 페이지 URL", example = "https://www.letskorail.com/event/discount")
    String url
) {

    public static IncentiveResponse from(Incentive incentive) {
        return new IncentiveResponse(incentive.getId(), incentive.getTitle(), incentive.getUrl());
    }
}
