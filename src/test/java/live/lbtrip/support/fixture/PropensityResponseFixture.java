package live.lbtrip.support.fixture;

import live.lbtrip.domain.propensity.dto.response.PropensityResponse;

public final class PropensityResponseFixture {

    public static final String TYPE = "실속형 로컬 감성 여행자";
    public static final String DESCRIPTION = "럭셔리보다 실속을, 유명 명소보다 골목 상권을, 구경보다 직접 체험을 즐기는 세대 동행 여행자예요.";

    private PropensityResponseFixture() {
    }

    public static PropensityResponse.InnerPropensityResultResponse propensityResult() {
        return new PropensityResponse.InnerPropensityResultResponse(TYPE, DESCRIPTION);
    }

    public static PropensityResponse propensityResponse() {
        return new PropensityResponse(
            propensityResult(),
            new PropensityResponse.InnerPreferenceResponse(
                PropensityRequestFixture.LOCALITY,
                PropensityRequestFixture.FRUGALITY,
                PropensityRequestFixture.EXPERIENTIALITY,
                PropensityRequestFixture.VITALITY,
                PropensityRequestFixture.SOCIALITY
            ),
            new PropensityResponse.InnerValueConsumptionResponse(
                PropensityRequestFixture.ACCOMMODATION,
                PropensityRequestFixture.FOOD,
                PropensityRequestFixture.EXPERIENCE,
                PropensityRequestFixture.TRANSPORTATION,
                PropensityRequestFixture.CAFE_EXHIBITION
            )
        );
    }
}
