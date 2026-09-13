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
    @NotBlank(message = "{validation.incentiveTitle.required}")
    @Size(max = 200, message = "{validation.incentiveTitle.size}")
    String title,

    @Schema(description = "영문 행사 제목. 없으면 영어 응답에서 한글 제목으로 대체됩니다.", nullable = true,
        example = "KTX Discount for Depopulation Areas")
    @Size(max = 200, message = "{validation.incentiveTitleEn.size}")
    String titleEn,

    @Schema(description = "행사 페이지 URL", example = "https://www.letskorail.com/event/discount", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.incentiveUrl.required}")
    @Size(max = 500, message = "{validation.incentiveUrl.size}")
    String url,

    @Schema(description = "행사 부가 설명", example = "코레일 공식 채널로 이동")
    @Size(max = 200, message = "{validation.incentiveDescription.size}")
    String description,

    @Schema(description = "영문 행사 부가 설명. 없으면 영어 응답에서 한글 설명으로 대체됩니다.", nullable = true,
        example = "Opens the official Korail channel")
    @Size(max = 200, message = "{validation.incentiveDescriptionEn.size}")
    String descriptionEn,

    @Schema(description = "혜택 시작일", example = "2026-07-01", requiredMode = REQUIRED)
    @NotNull(message = "{validation.incentiveStartDate.required}")
    LocalDate startDate,

    @Schema(description = "혜택 종료일. null이면 종료일 없이 유지됩니다.", example = "2026-08-31", nullable = true)
    LocalDate endDate,

    @Schema(description = "적용 지역 후보 ID 목록", requiredMode = REQUIRED)
    @NotEmpty(message = "{validation.incentiveRegions.required}")
    List<Long> regionCandidateIds
) {
}
