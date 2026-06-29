package live.lbtrip.support.fixture;

import live.lbtrip.domain.propensity.dto.request.PropensityRequest;

public final class PropensityRequestFixture {

    public static final int LOCALITY = 4;
    public static final int FRUGALITY = 5;
    public static final int FLEXIBILITY = 3;
    public static final int EXPERIENTIALITY = 4;
    public static final int VITALITY = 2;
    public static final int SOCIALITY = 1;

    public static final int UPDATED_LOCALITY = 2;
    public static final int UPDATED_FRUGALITY = 3;
    public static final int UPDATED_FLEXIBILITY = 4;
    public static final int UPDATED_EXPERIENTIALITY = 5;
    public static final int UPDATED_VITALITY = 1;
    public static final int UPDATED_SOCIALITY = 2;

    private PropensityRequestFixture() {
    }

    public static PropensityRequest propensityRequest() {
        return new PropensityRequest(
            LOCALITY,
            FRUGALITY,
            FLEXIBILITY,
            EXPERIENTIALITY,
            VITALITY,
            SOCIALITY
        );
    }

    public static PropensityRequest updatedPropensityRequest() {
        return new PropensityRequest(
            UPDATED_LOCALITY,
            UPDATED_FRUGALITY,
            UPDATED_FLEXIBILITY,
            UPDATED_EXPERIENTIALITY,
            UPDATED_VITALITY,
            UPDATED_SOCIALITY
        );
    }
}
