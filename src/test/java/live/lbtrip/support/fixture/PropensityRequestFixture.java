package live.lbtrip.support.fixture;

import live.lbtrip.domain.propensity.dto.request.PropensityRequest;

public final class PropensityRequestFixture {

    public static final int LOCALITY = 4;
    public static final int FRUGALITY = 5;
    public static final int EXPERIENTIALITY = 4;
    public static final int VITALITY = 2;
    public static final int SOCIALITY = 4;
    public static final int ACCOMMODATION = 2;
    public static final int FOOD = 4;
    public static final int EXPERIENCE = 5;
    public static final int TRANSPORTATION = 2;
    public static final int CAFE_EXHIBITION = 4;

    public static final int UPDATED_LOCALITY = 2;
    public static final int UPDATED_FRUGALITY = 3;
    public static final int UPDATED_EXPERIENTIALITY = 5;
    public static final int UPDATED_VITALITY = 1;
    public static final int UPDATED_SOCIALITY = 2;
    public static final int UPDATED_ACCOMMODATION = 5;
    public static final int UPDATED_FOOD = 2;
    public static final int UPDATED_EXPERIENCE = 3;
    public static final int UPDATED_TRANSPORTATION = 4;
    public static final int UPDATED_CAFE_EXHIBITION = 1;

    private PropensityRequestFixture() {
    }

    public static PropensityRequest propensityRequest() {
        return new PropensityRequest(
            new PropensityRequest.InnerPreferenceRequest(
                LOCALITY,
                FRUGALITY,
                EXPERIENTIALITY,
                VITALITY,
                SOCIALITY
            ),
            new PropensityRequest.InnerValueConsumptionRequest(
                ACCOMMODATION,
                FOOD,
                EXPERIENCE,
                TRANSPORTATION,
                CAFE_EXHIBITION
            )
        );
    }

    public static PropensityRequest updatedPropensityRequest() {
        return new PropensityRequest(
            new PropensityRequest.InnerPreferenceRequest(
                UPDATED_LOCALITY,
                UPDATED_FRUGALITY,
                UPDATED_EXPERIENTIALITY,
                UPDATED_VITALITY,
                UPDATED_SOCIALITY
            ),
            new PropensityRequest.InnerValueConsumptionRequest(
                UPDATED_ACCOMMODATION,
                UPDATED_FOOD,
                UPDATED_EXPERIENCE,
                UPDATED_TRANSPORTATION,
                UPDATED_CAFE_EXHIBITION
            )
        );
    }
}
