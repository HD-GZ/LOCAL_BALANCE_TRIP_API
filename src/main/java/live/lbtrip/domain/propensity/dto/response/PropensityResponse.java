package live.lbtrip.domain.propensity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.model.ValueConsumption;

public record PropensityResponse(
    @Schema(description = "취향 결과(라벨 + 설명)")
    InnerPropensityResultResponse propensityResult,

    @Schema(description = "5축 취향 진단 점수")
    InnerPreferenceResponse preference,

    @Schema(description = "가치소비 점수")
    InnerValueConsumptionResponse valueConsumption
) {

    public record InnerPropensityResultResponse(
        @Schema(description = "진단 유형 라벨. \"{별칭} ({코드})\" 형식.", example = "찐로컬 탐험가 (LVEAI)")
        String type,

        @Schema(description = "진단 유형 설명", example = "로컬의 구석구석을 발로 뛰며 직접 체험하는 실속파 혼행 여행자예요.")
        String description,

        @Schema(description = "유형 캐릭터 이미지 URL. 이미지 미등록 시 null.", example = "https://stage.images.lb-trip.live/travel-profiles/lveai.png", nullable = true)
        String imageUrl
    ) {
    }

    public record InnerPreferenceResponse(
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

    public record InnerValueConsumptionResponse(
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

    public static PropensityResponse of(Propensity propensity, TravelProfile travelProfile, String imageUrl) {
        Preference preference = propensity.getPreference();
        ValueConsumption valueConsumption = propensity.getValueConsumption();

        return new PropensityResponse(
            new InnerPropensityResultResponse(
                "%s (%s)".formatted(travelProfile.getNickname(), travelProfile.getCode()),
                travelProfile.getDescription(),
                imageUrl
            ),
            new InnerPreferenceResponse(
                preference.getLocality(),
                preference.getFrugality(),
                preference.getExperientiality(),
                preference.getVitality(),
                preference.getSociality()
            ),
            new InnerValueConsumptionResponse(
                valueConsumption.getAccommodation(),
                valueConsumption.getFood(),
                valueConsumption.getExperience(),
                valueConsumption.getTransportation(),
                valueConsumption.getCafeExhibition()
            )
        );
    }
}
