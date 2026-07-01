package live.lbtrip.domain.propensity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.domain.propensity.model.Propensity;

public record PropensityResponse(
    @Schema(description = "진단 결과(라벨 + 설명)")
    Result result,

    @Schema(description = "5축 취향 진단 점수")
    Preference preference,

    @Schema(description = "가치소비 점수")
    ValueConsumption valueConsumption
) {

    public record Result(
        @Schema(description = "진단 유형 라벨", example = "실속형 로컬 감성 여행자")
        String type,

        @Schema(description = "진단 유형 설명", example = "럭셔리보다 실속을, 유명 명소보다 골목 상권을, 빡빡한 일정보다 감성 여백을 즐기는 1인 여행자예요.")
        String description
    ) {
    }

    public record Preference(
        @Schema(description = "여행지 선택 점수. 1(핫플·유명 명소) ~ 5(로컬·골목 상권).", example = "4")
        int locality,

        @Schema(description = "소비 기준 점수. 1(럭셔리·프리미엄) ~ 5(실속·가성비).", example = "5")
        int frugality,

        @Schema(description = "활동 방식 점수. 1(관람형·보고 즐기기) ~ 5(생활 체험·직접 해보기).", example = "4")
        int experientiality,

        @Schema(description = "여행 스타일 점수. 1(휴식형·느긋한 쉼) ~ 5(활동형·부지런한 일정).", example = "2")
        int vitality,

        @Schema(description = "동행 유형 점수. 1(혼행·나 홀로) ~ 5(세대 동행·가족).", example = "4")
        int sociality
    ) {
    }

    public record ValueConsumption(
        @Schema(description = "숙소 가치소비 점수. 1(아끼기) ~ 5(투자).", example = "2")
        int accommodation,

        @Schema(description = "음식 가치소비 점수. 1(아끼기) ~ 5(투자).", example = "4")
        int food,

        @Schema(description = "체험 가치소비 점수. 1(아끼기) ~ 5(투자).", example = "5")
        int experience,

        @Schema(description = "이동 가치소비 점수. 1(아끼기) ~ 5(투자).", example = "2")
        int transportation,

        @Schema(description = "카페·전시 가치소비 점수. 1(아끼기) ~ 5(투자).", example = "4")
        int cafeExhibition
    ) {
    }

    public static PropensityResponse of(Propensity propensity, Result result) {
        return new PropensityResponse(
            result,
            new Preference(
                propensity.getLocality(),
                propensity.getFrugality(),
                propensity.getExperientiality(),
                propensity.getVitality(),
                propensity.getSociality()
            ),
            new ValueConsumption(
                propensity.getAccommodation(),
                propensity.getFood(),
                propensity.getExperience(),
                propensity.getTransportation(),
                propensity.getCafeExhibition()
            )
        );
    }
}
