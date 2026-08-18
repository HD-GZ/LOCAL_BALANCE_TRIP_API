package live.lbtrip.domain.propensity.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.ValueConsumption;

public record PropensityRequest(
    @Schema(description = "5축 취향 진단 점수", requiredMode = REQUIRED)
    @NotNull(message = "{validation.preference.required}")
    @Valid
    InnerPreferenceRequest preference,

    @Schema(description = "가치소비 점수", requiredMode = REQUIRED)
    @NotNull(message = "{validation.valueConsumption.required}")
    @Valid
    InnerValueConsumptionRequest valueConsumption
) {

    public Preference toPreference() {
        return Preference.of(
            preference.locality(),
            preference.frugality(),
            preference.experientiality(),
            preference.vitality(),
            preference.sociality()
        );
    }

    public ValueConsumption toValueConsumption() {
        return ValueConsumption.of(
            valueConsumption.accommodation(),
            valueConsumption.food(),
            valueConsumption.experience(),
            valueConsumption.transportation(),
            valueConsumption.cafeExhibition()
        );
    }

    public record InnerPreferenceRequest(
        @Schema(description = "여행지 선택 점수. 1(핫플·유명 명소) ~ 5(로컬·골목 상권), 정수.", example = "4", requiredMode = REQUIRED)
        @NotNull(message = "{validation.locality.required}")
        @Min(value = 1, message = "{validation.locality.min}")
        @Max(value = 5, message = "{validation.locality.max}")
        Integer locality,

        @Schema(description = "소비 기준 점수. 1(럭셔리·프리미엄) ~ 5(실속·가성비), 정수.", example = "5", requiredMode = REQUIRED)
        @NotNull(message = "{validation.frugality.required}")
        @Min(value = 1, message = "{validation.frugality.min}")
        @Max(value = 5, message = "{validation.frugality.max}")
        Integer frugality,

        @Schema(description = "활동 방식 점수. 1(관람형·보고 즐기기) ~ 5(생활 체험·직접 해보기), 정수.", example = "4", requiredMode = REQUIRED)
        @NotNull(message = "{validation.experientiality.required}")
        @Min(value = 1, message = "{validation.experientiality.min}")
        @Max(value = 5, message = "{validation.experientiality.max}")
        Integer experientiality,

        @Schema(description = "여행 스타일 점수. 1(휴식형·느긋한 쉼) ~ 5(활동형·부지런한 일정), 정수.", example = "2", requiredMode = REQUIRED)
        @NotNull(message = "{validation.vitality.required}")
        @Min(value = 1, message = "{validation.vitality.min}")
        @Max(value = 5, message = "{validation.vitality.max}")
        Integer vitality,

        @Schema(description = "동행 유형 점수. 1(혼행·나 홀로) ~ 5(세대 동행·가족), 정수.", example = "4", requiredMode = REQUIRED)
        @NotNull(message = "{validation.sociality.required}")
        @Min(value = 1, message = "{validation.sociality.min}")
        @Max(value = 5, message = "{validation.sociality.max}")
        Integer sociality
    ) {
    }

    public record InnerValueConsumptionRequest(
        @Schema(description = "숙소 가치소비 점수. 1(아끼기) ~ 5(투자), 정수.", example = "2", requiredMode = REQUIRED)
        @NotNull(message = "{validation.accommodation.required}")
        @Min(value = 1, message = "{validation.accommodation.min}")
        @Max(value = 5, message = "{validation.accommodation.max}")
        Integer accommodation,

        @Schema(description = "음식 가치소비 점수. 1(아끼기) ~ 5(투자), 정수.", example = "4", requiredMode = REQUIRED)
        @NotNull(message = "{validation.food.required}")
        @Min(value = 1, message = "{validation.food.min}")
        @Max(value = 5, message = "{validation.food.max}")
        Integer food,

        @Schema(description = "체험 가치소비 점수. 1(아끼기) ~ 5(투자), 정수.", example = "5", requiredMode = REQUIRED)
        @NotNull(message = "{validation.experience.required}")
        @Min(value = 1, message = "{validation.experience.min}")
        @Max(value = 5, message = "{validation.experience.max}")
        Integer experience,

        @Schema(description = "이동 가치소비 점수. 1(아끼기) ~ 5(투자), 정수.", example = "2", requiredMode = REQUIRED)
        @NotNull(message = "{validation.transportation.required}")
        @Min(value = 1, message = "{validation.transportation.min}")
        @Max(value = 5, message = "{validation.transportation.max}")
        Integer transportation,

        @Schema(description = "카페·전시 가치소비 점수. 1(아끼기) ~ 5(투자), 정수.", example = "4", requiredMode = REQUIRED)
        @NotNull(message = "{validation.cafeExhibition.required}")
        @Min(value = 1, message = "{validation.cafeExhibition.min}")
        @Max(value = 5, message = "{validation.cafeExhibition.max}")
        Integer cafeExhibition
    ) {
    }
}
