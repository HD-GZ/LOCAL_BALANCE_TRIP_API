package live.lbtrip.admin.incentive.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminIncentiveRequest(
    @Schema(description = "행사 제목", example = "KTX 인구감소지역 할인", requiredMode = REQUIRED)
    @NotBlank(message = "행사 제목은 필수입니다.")
    @Size(max = 200, message = "행사 제목은 200자 이하여야 합니다.")
    String title,

    @Schema(description = "행사 페이지 URL", example = "https://www.letskorail.com/event/discount", requiredMode = REQUIRED)
    @NotBlank(message = "행사 페이지 URL은 필수입니다.")
    @Size(max = 500, message = "행사 페이지 URL은 500자 이하여야 합니다.")
    String url,

    @Schema(description = "행사 부가 설명", example = "코레일 공식 채널로 이동")
    @Size(max = 200, message = "행사 부가 설명은 200자 이하여야 합니다.")
    String description,

    @Schema(description = "혜택 시작일", example = "2026-07-01", requiredMode = REQUIRED)
    @NotNull(message = "혜택 시작일은 필수입니다.")
    LocalDate startDate,

    @Schema(description = "혜택 종료일. null이면 종료일 없이 유지됩니다.", example = "2026-08-31", nullable = true)
    LocalDate endDate,

    @Schema(description = "적용 지역 후보 ID 목록", requiredMode = REQUIRED)
    @NotEmpty(message = "적용 지역 목록은 필수입니다.")
    List<Long> regionCandidateIds
) {
}
