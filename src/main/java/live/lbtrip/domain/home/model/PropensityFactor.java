package live.lbtrip.domain.home.model;

import java.util.function.ToIntBiFunction;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.ValueConsumption;

public enum PropensityFactor {

    LOCALITY((p, v) -> p.getLocality()),
    FRUGALITY((p, v) -> p.getFrugality()),
    EXPERIENTIALITY((p, v) -> p.getExperientiality()),
    VITALITY((p, v) -> p.getVitality()),
    SOCIALITY((p, v) -> p.getSociality()),
    ACCOMMODATION((p, v) -> v.getAccommodation()),
    FOOD((p, v) -> v.getFood()),
    EXPERIENCE((p, v) -> v.getExperience()),
    TRANSPORTATION((p, v) -> v.getTransportation()),
    CAFE_EXHIBITION((p, v) -> v.getCafeExhibition());

    private final ToIntBiFunction<Preference, ValueConsumption> extractor;

    PropensityFactor(ToIntBiFunction<Preference, ValueConsumption> extractor) {
        this.extractor = extractor;
    }

    public int score(Preference preference, ValueConsumption valueConsumption) {
        return extractor.applyAsInt(preference, valueConsumption);
    }
}
