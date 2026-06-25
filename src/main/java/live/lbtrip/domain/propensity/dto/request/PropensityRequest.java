package live.lbtrip.domain.propensity.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PropensityRequest(
    @Schema(description = "여행지 선택 점수. 1(핫플·유명 명소) ~ 5(로컬·골목 상권), 정수.", example = "4", requiredMode = REQUIRED)
    @NotNull(message = "여행지 선택 점수는 필수입니다.")
    @Min(value = 1, message = "여행지 선택 점수는 1 이상이어야 합니다.")
    @Max(value = 5, message = "여행지 선택 점수는 5 이하여야 합니다.")
    Integer locality,

    @Schema(description = "소비 기준 점수. 1(럭셔리·프리미엄) ~ 5(실속·가성비), 정수.", example = "5", requiredMode = REQUIRED)
    @NotNull(message = "소비 기준 점수는 필수입니다.")
    @Min(value = 1, message = "소비 기준 점수는 1 이상이어야 합니다.")
    @Max(value = 5, message = "소비 기준 점수는 5 이하여야 합니다.")
    Integer frugality,

    @Schema(description = "코스 설계 점수. 1(AI 자동) ~ 5(감성 여백·즉흥), 정수.", example = "3", requiredMode = REQUIRED)
    @NotNull(message = "코스 설계 점수는 필수입니다.")
    @Min(value = 1, message = "코스 설계 점수는 1 이상이어야 합니다.")
    @Max(value = 5, message = "코스 설계 점수는 5 이하여야 합니다.")
    Integer flexibility,

    @Schema(description = "활동 방식 점수. 1(관람형·보고 즐기기) ~ 5(생활 체험·직접 해보기), 정수.", example = "4", requiredMode = REQUIRED)
    @NotNull(message = "활동 방식 점수는 필수입니다.")
    @Min(value = 1, message = "활동 방식 점수는 1 이상이어야 합니다.")
    @Max(value = 5, message = "활동 방식 점수는 5 이하여야 합니다.")
    Integer experientiality,

    @Schema(description = "여행 강도 점수. 1(휴식형·느긋한 쉼) ~ 5(활동형·부지런한 일정), 정수.", example = "2", requiredMode = REQUIRED)
    @NotNull(message = "여행 강도 점수는 필수입니다.")
    @Min(value = 1, message = "여행 강도 점수는 1 이상이어야 합니다.")
    @Max(value = 5, message = "여행 강도 점수는 5 이하여야 합니다.")
    Integer vitality,

    @Schema(description = "동행 유형 점수. 1(혼행·나 홀로) ~ 5(세대 동행·가족), 정수.", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "동행 유형 점수는 필수입니다.")
    @Min(value = 1, message = "동행 유형 점수는 1 이상이어야 합니다.")
    @Max(value = 5, message = "동행 유형 점수는 5 이하여야 합니다.")
    Integer sociality
) {
}
