package live.lbtrip.support.fixture;

import live.lbtrip.domain.admin.incentive.dto.request.IncentiveRequest;

public final class IncentiveRequestFixture {

    public static final String TITLE = "KTX 인구감소지역 할인";
    public static final String URL = "https://www.letskorail.com/event/discount";
    public static final String UPDATED_TITLE = "디지털 관광주민증";
    public static final String UPDATED_URL = "https://dtrc.visitkorea.or.kr";

    private IncentiveRequestFixture() {
    }

    public static IncentiveRequest incentiveRequest() {
        return new IncentiveRequest(TITLE, URL);
    }

    public static IncentiveRequest updatedIncentiveRequest() {
        return new IncentiveRequest(UPDATED_TITLE, UPDATED_URL);
    }
}
