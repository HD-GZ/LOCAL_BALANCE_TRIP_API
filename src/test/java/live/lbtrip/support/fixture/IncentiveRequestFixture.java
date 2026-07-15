package live.lbtrip.support.fixture;

import java.util.List;

import live.lbtrip.domain.admin.incentive.dto.request.IncentiveRequest;

public final class IncentiveRequestFixture {

    public static final String TITLE = "KTX 인구감소지역 할인";
    public static final String URL = "https://www.letskorail.com/event/discount";
    public static final String DESCRIPTION = "코레일 공식 채널로 이동";
    public static final String UPDATED_TITLE = "디지털 관광주민증";
    public static final String UPDATED_URL = "https://dtrc.visitkorea.or.kr";
    public static final String UPDATED_DESCRIPTION = "담양 가맹점 12곳 할인";
    public static final String LDONG_REGN_CD = "46";
    public static final String LDONG_SIGNGU_CD = "710";

    private IncentiveRequestFixture() {
    }

    public static IncentiveRequest incentiveRequest() {
        return new IncentiveRequest(TITLE, URL, DESCRIPTION, regions());
    }

    public static IncentiveRequest updatedIncentiveRequest() {
        return new IncentiveRequest(UPDATED_TITLE, UPDATED_URL, UPDATED_DESCRIPTION, regions());
    }

    public static List<IncentiveRequest.RegionRequest> regions() {
        return List.of(new IncentiveRequest.RegionRequest(LDONG_REGN_CD, LDONG_SIGNGU_CD));
    }
}
