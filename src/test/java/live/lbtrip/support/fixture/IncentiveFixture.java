package live.lbtrip.support.fixture;

import live.lbtrip.domain.admin.incentive.model.Incentive;

public final class IncentiveFixture {

    private IncentiveFixture() {
    }

    public static Incentive incentive() {
        return Incentive.create(IncentiveRequestFixture.TITLE, IncentiveRequestFixture.URL);
    }
}
