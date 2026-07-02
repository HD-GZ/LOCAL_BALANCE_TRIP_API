package live.lbtrip.support.fixture;

import live.lbtrip.domain.propensity.model.PropensityBucket;
import live.lbtrip.domain.propensity.model.TravelProfile;

public final class TravelProfileFixture {

    private TravelProfileFixture() {
    }

    public static TravelProfile travelProfile() {
        return TravelProfile.create(
            PropensityBucket.HIGH,
            PropensityBucket.HIGH,
            PropensityBucket.LOW,
            PropensityBucket.LOW,
            PropensityBucket.HIGH,
            PropensityBucket.LOW,
            PropensityBucket.HIGH,
            PropensityBucket.HIGH,
            PropensityBucket.LOW,
            PropensityBucket.HIGH,
            PropensityResponseFixture.TYPE,
            PropensityResponseFixture.DESCRIPTION
        );
    }
}
