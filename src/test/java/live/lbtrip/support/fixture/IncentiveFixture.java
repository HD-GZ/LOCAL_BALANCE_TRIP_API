package live.lbtrip.support.fixture;

import java.util.List;

import live.lbtrip.domain.admin.incentive.model.Incentive;
import live.lbtrip.domain.admin.incentive.model.IncentiveRegion;

public final class IncentiveFixture {

    private IncentiveFixture() {
    }

    public static Incentive incentive() {
        Incentive incentive = Incentive.create(
            IncentiveRequestFixture.TITLE, IncentiveRequestFixture.URL, IncentiveRequestFixture.DESCRIPTION);
        incentive.replaceRegions(List.of(IncentiveRegion.create(
            IncentiveRequestFixture.LDONG_REGN_CD, IncentiveRequestFixture.LDONG_SIGNGU_CD)));
        return incentive;
    }
}
