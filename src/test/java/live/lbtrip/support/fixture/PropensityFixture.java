package live.lbtrip.support.fixture;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.user.model.User;

public final class PropensityFixture {

    private PropensityFixture() {
    }

    public static Preference preference() {
        return PropensityRequestFixture.propensityRequest().toPreference();
    }

    public static ValueConsumption valueConsumption() {
        return PropensityRequestFixture.propensityRequest().toValueConsumption();
    }

    public static Preference updatedPreference() {
        return PropensityRequestFixture.updatedPropensityRequest().toPreference();
    }

    public static ValueConsumption updatedValueConsumption() {
        return PropensityRequestFixture.updatedPropensityRequest().toValueConsumption();
    }

    public static Propensity propensity() {
        return propensity(UserFixture.user(), preference(), valueConsumption());
    }

    public static Propensity propensity(User user, Preference preference, ValueConsumption valueConsumption) {
        return Propensity.create(user, preference, valueConsumption);
    }
}
