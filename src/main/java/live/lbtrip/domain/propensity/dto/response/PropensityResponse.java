package live.lbtrip.domain.propensity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PropensityResponse(
    @Schema(description = "진단 결과(라벨 + 설명)")
    Result result,

    @Schema(description = "여행지 선택 점수. 1(핫플·유명 명소) ~ 5(로컬·골목 상권).", example = "4")
    int locality,

    @Schema(description = "소비 기준 점수. 1(럭셔리·프리미엄) ~ 5(실속·가성비).", example = "5")
    int frugality,

    @Schema(description = "코스 설계 점수. 1(AI 자동) ~ 5(감성 여백·즉흥).", example = "3")
    int flexibility,

    @Schema(description = "활동 방식 점수. 1(관람형·보고 즐기기) ~ 5(생활 체험·직접 해보기).", example = "4")
    int experientiality,

    @Schema(description = "여행 강도 점수. 1(휴식형·느긋한 쉼) ~ 5(활동형·부지런한 일정).", example = "2")
    int vitality,

    @Schema(description = "동행 유형 점수. 1(혼행·나 홀로) ~ 5(세대 동행·가족).", example = "1")
    int sociality
) {

    public record Result(
        @Schema(description = "진단 유형 라벨", example = "실속형 로컬 감성 여행자")
        String type,

        @Schema(description = "진단 유형 설명", example = "럭셔리보다 실속을, 유명 명소보다 골목 상권을, 빡빡한 일정보다 감성 여백을 즐기는 1인 여행자예요.")
        String description
    ) {
    }
}
