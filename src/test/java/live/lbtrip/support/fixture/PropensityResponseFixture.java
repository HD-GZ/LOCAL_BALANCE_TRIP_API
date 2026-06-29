package live.lbtrip.support.fixture;

import live.lbtrip.domain.propensity.dto.response.PropensityResponse;

public final class PropensityResponseFixture {

    public static final String TYPE = "실속형 로컬 감성 여행자";
    public static final String DESCRIPTION = "럭셔리보다 실속을, 유명 명소보다 골목 상권을, 빡빡한 일정보다 감성 여백을 즐기는 1인 여행자예요.";

    private PropensityResponseFixture() {
    }

    public static PropensityResponse.Result result() {
        return new PropensityResponse.Result(TYPE, DESCRIPTION);
    }

    public static PropensityResponse propensityResponse() {
        return new PropensityResponse(
            result(),
            PropensityRequestFixture.LOCALITY,
            PropensityRequestFixture.FRUGALITY,
            PropensityRequestFixture.FLEXIBILITY,
            PropensityRequestFixture.EXPERIENTIALITY,
            PropensityRequestFixture.VITALITY,
            PropensityRequestFixture.SOCIALITY
        );
    }
}
