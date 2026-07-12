package live.lbtrip.domain.admin.incentive.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IncentiveRequest(
    @Schema(description = "행사 제목", example = "KTX 인구감소지역 할인", requiredMode = REQUIRED)
    @NotBlank(message = "행사 제목은 필수입니다.")
    @Size(max = 200, message = "행사 제목은 200자 이하여야 합니다.")
    String title,

    @Schema(description = "행사 페이지 URL", example = "https://www.letskorail.com/event/discount", requiredMode = REQUIRED)
    @NotBlank(message = "행사 페이지 URL은 필수입니다.")
    @Size(max = 500, message = "행사 페이지 URL은 500자 이하여야 합니다.")
    String url
) {
}
