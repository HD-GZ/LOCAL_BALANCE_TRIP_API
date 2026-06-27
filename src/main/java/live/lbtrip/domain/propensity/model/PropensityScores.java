package live.lbtrip.domain.propensity.model;

import live.lbtrip.domain.propensity.dto.request.PropensityRequest;

public record PropensityScores(
    int locality,
    int frugality,
    int flexibility,
    int experientiality,
    int vitality,
    int sociality
) {

    public static PropensityScores from(PropensityRequest request) {
        return new PropensityScores(
            request.locality(),
            request.frugality(),
            request.flexibility(),
            request.experientiality(),
            request.vitality(),
            request.sociality()
        );
    }

    public static PropensityScores from(Propensity propensity) {
        return new PropensityScores(
            propensity.getLocality(),
            propensity.getFrugality(),
            propensity.getFlexibility(),
            propensity.getExperientiality(),
            propensity.getVitality(),
            propensity.getSociality()
        );
    }
}
