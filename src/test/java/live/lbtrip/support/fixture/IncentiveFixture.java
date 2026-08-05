package live.lbtrip.support.fixture;

import java.util.List;

import live.lbtrip.domain.incentive.model.Incentive;
import live.lbtrip.domain.incentive.model.IncentiveRegion;

public final class IncentiveFixture {

    private IncentiveFixture() {
    }

    public static Incentive incentive() {
        Incentive incentive = Incentive.create(
            AdminIncentiveRequestFixture.TITLE,
            AdminIncentiveRequestFixture.URL,
            AdminIncentiveRequestFixture.DESCRIPTION,
            AdminIncentiveRequestFixture.START_DATE,
            AdminIncentiveRequestFixture.END_DATE);
        incentive.replaceRegions(List.of(IncentiveRegion.create(
            AdminIncentiveRequestFixture.LDONG_REGN_CD,
            AdminIncentiveRequestFixture.LDONG_SIGNGU_CD)));
        return incentive;
    }
}
