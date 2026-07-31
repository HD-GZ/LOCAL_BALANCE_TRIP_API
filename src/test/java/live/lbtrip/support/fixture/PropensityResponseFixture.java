package live.lbtrip.support.fixture;

import live.lbtrip.domain.propensity.dto.response.PropensityResponse;

public final class PropensityResponseFixture {

    public static final String TYPE = "%s (%s)".formatted(TravelProfileFixture.NICKNAME, TravelProfileFixture.CODE);
    public static final String DESCRIPTION = TravelProfileFixture.DESCRIPTION;
    public static final String IMAGE_URL = "https://stage.images.lb-trip.live/" + TravelProfileFixture.IMAGE_KEY;

    private PropensityResponseFixture() {
    }

    public static PropensityResponse propensityResponse() {
        return new PropensityResponse(
            new PropensityResponse.InnerPropensityResultResponse(TYPE, DESCRIPTION, IMAGE_URL),
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
