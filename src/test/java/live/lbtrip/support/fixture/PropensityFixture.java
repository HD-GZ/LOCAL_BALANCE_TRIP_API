package live.lbtrip.support.fixture;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.user.model.User;

public final class PropensityFixture {

    private PropensityFixture() {
    }

    public static Preference preference() {
        return Preference.from(PropensityRequestFixture.propensityRequest().preference());
    }

    public static ValueConsumption valueConsumption() {
        return ValueConsumption.from(PropensityRequestFixture.propensityRequest().valueConsumption());
    }

    public static Preference updatedPreference() {
        return Preference.from(PropensityRequestFixture.updatedPropensityRequest().preference());
    }

    public static ValueConsumption updatedValueConsumption() {
        return ValueConsumption.from(PropensityRequestFixture.updatedPropensityRequest().valueConsumption());
    }

    public static Propensity propensity() {
        return propensity(UserFixture.user(), preference(), valueConsumption());
    }

    public static Propensity propensity(User user, Preference preference, ValueConsumption valueConsumption) {
        return Propensity.create(user, preference, valueConsumption);
    }
}
