package live.lbtrip.support.fixture;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.PropensityScores;
import live.lbtrip.domain.user.model.User;

public final class PropensityFixture {

    private PropensityFixture() {
    }

    public static PropensityScores scores() {
        return PropensityScores.from(PropensityRequestFixture.propensityRequest());
    }

    public static PropensityScores updatedScores() {
        return PropensityScores.from(PropensityRequestFixture.updatedPropensityRequest());
    }

    public static Propensity propensity() {
        return propensity(UserFixture.user(), scores());
    }

    public static Propensity propensity(User user, PropensityScores scores) {
        return Propensity.create(user, scores);
    }
}
