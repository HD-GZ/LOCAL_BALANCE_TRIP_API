package live.lbtrip.domain.propensity.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PropensityRequest(
    @Schema(description = "5축 취향 진단 점수", requiredMode = REQUIRED)
    @NotNull(message = "취향 진단 점수는 필수입니다.")
    @Valid
    InnerPreferenceRequest preference,

    @Schema(description = "가치소비 점수", requiredMode = REQUIRED)
    @NotNull(message = "가치소비 점수는 필수입니다.")
    @Valid
    InnerValueConsumptionRequest valueConsumption
) {

    public record InnerPreferenceRequest(
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

        @Schema(description = "활동 방식 점수. 1(관람형·보고 즐기기) ~ 5(생활 체험·직접 해보기), 정수.", example = "4", requiredMode = REQUIRED)
        @NotNull(message = "활동 방식 점수는 필수입니다.")
        @Min(value = 1, message = "활동 방식 점수는 1 이상이어야 합니다.")
        @Max(value = 5, message = "활동 방식 점수는 5 이하여야 합니다.")
        Integer experientiality,

        @Schema(description = "여행 스타일 점수. 1(휴식형·느긋한 쉼) ~ 5(활동형·부지런한 일정), 정수.", example = "2", requiredMode = REQUIRED)
        @NotNull(message = "여행 스타일 점수는 필수입니다.")
        @Min(value = 1, message = "여행 스타일 점수는 1 이상이어야 합니다.")
        @Max(value = 5, message = "여행 스타일 점수는 5 이하여야 합니다.")
        Integer vitality,

        @Schema(description = "동행 유형 점수. 1(혼행·나 홀로) ~ 5(세대 동행·가족), 정수.", example = "4", requiredMode = REQUIRED)
        @NotNull(message = "동행 유형 점수는 필수입니다.")
        @Min(value = 1, message = "동행 유형 점수는 1 이상이어야 합니다.")
        @Max(value = 5, message = "동행 유형 점수는 5 이하여야 합니다.")
        Integer sociality
    ) {
    }

    public record InnerValueConsumptionRequest(
        @Schema(description = "숙소 가치소비 점수. 1(아끼기) ~ 5(투자), 정수.", example = "2", requiredMode = REQUIRED)
        @NotNull(message = "숙소 가치소비 점수는 필수입니다.")
        @Min(value = 1, message = "숙소 가치소비 점수는 1 이상이어야 합니다.")
        @Max(value = 5, message = "숙소 가치소비 점수는 5 이하여야 합니다.")
        Integer accommodation,

        @Schema(description = "음식 가치소비 점수. 1(아끼기) ~ 5(투자), 정수.", example = "4", requiredMode = REQUIRED)
        @NotNull(message = "음식 가치소비 점수는 필수입니다.")
        @Min(value = 1, message = "음식 가치소비 점수는 1 이상이어야 합니다.")
        @Max(value = 5, message = "음식 가치소비 점수는 5 이하여야 합니다.")
        Integer food,

        @Schema(description = "체험 가치소비 점수. 1(아끼기) ~ 5(투자), 정수.", example = "5", requiredMode = REQUIRED)
        @NotNull(message = "체험 가치소비 점수는 필수입니다.")
        @Min(value = 1, message = "체험 가치소비 점수는 1 이상이어야 합니다.")
        @Max(value = 5, message = "체험 가치소비 점수는 5 이하여야 합니다.")
        Integer experience,

        @Schema(description = "이동 가치소비 점수. 1(아끼기) ~ 5(투자), 정수.", example = "2", requiredMode = REQUIRED)
        @NotNull(message = "이동 가치소비 점수는 필수입니다.")
        @Min(value = 1, message = "이동 가치소비 점수는 1 이상이어야 합니다.")
        @Max(value = 5, message = "이동 가치소비 점수는 5 이하여야 합니다.")
        Integer transportation,

        @Schema(description = "카페·전시 가치소비 점수. 1(아끼기) ~ 5(투자), 정수.", example = "4", requiredMode = REQUIRED)
        @NotNull(message = "카페·전시 가치소비 점수는 필수입니다.")
        @Min(value = 1, message = "카페·전시 가치소비 점수는 1 이상이어야 합니다.")
        @Max(value = 5, message = "카페·전시 가치소비 점수는 5 이하여야 합니다.")
        Integer cafeExhibition
    ) {
    }
}
