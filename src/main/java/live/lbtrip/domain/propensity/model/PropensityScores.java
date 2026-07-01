package live.lbtrip.domain.propensity.model;

import live.lbtrip.domain.propensity.dto.request.PropensityRequest;

public record PropensityScores(
    int locality,
    int frugality,
    int experientiality,
    int vitality,
    int sociality,
    int accommodation,
    int food,
    int experience,
    int transportation,
    int cafeExhibition
) {

    public static PropensityScores from(PropensityRequest request) {
        return new PropensityScores(
            request.preference().locality(),
            request.preference().frugality(),
            request.preference().experientiality(),
            request.preference().vitality(),
            request.preference().sociality(),
            request.valueConsumption().accommodation(),
            request.valueConsumption().food(),
            request.valueConsumption().experience(),
            request.valueConsumption().transportation(),
            request.valueConsumption().cafeExhibition()
        );
    }

    public static PropensityScores from(Propensity propensity) {
        return new PropensityScores(
            propensity.getLocality(),
            propensity.getFrugality(),
            propensity.getExperientiality(),
            propensity.getVitality(),
            propensity.getSociality(),
            propensity.getAccommodation(),
            propensity.getFood(),
            propensity.getExperience(),
            propensity.getTransportation(),
            propensity.getCafeExhibition()
        );
    }
}
