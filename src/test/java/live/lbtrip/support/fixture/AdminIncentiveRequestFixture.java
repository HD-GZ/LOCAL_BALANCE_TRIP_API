package live.lbtrip.support.fixture;

import java.time.LocalDate;
import java.util.List;

import live.lbtrip.admin.incentive.dto.request.AdminIncentiveRequest;

public final class AdminIncentiveRequestFixture {

    public static final String TITLE = "KTX 인구감소지역 할인";
    public static final String URL = "https://www.letskorail.com/event/discount";
    public static final String DESCRIPTION = "코레일 공식 채널로 이동";
    public static final String UPDATED_TITLE = "디지털 관광주민증";
    public static final String UPDATED_URL = "https://dtrc.visitkorea.or.kr";
    public static final String UPDATED_DESCRIPTION = "담양 가맹점 12곳 할인";
    public static final LocalDate START_DATE = LocalDate.of(2026, 7, 1);
    public static final LocalDate END_DATE = LocalDate.of(2026, 8, 31);
    public static final LocalDate UPDATED_START_DATE = LocalDate.of(2026, 9, 1);
    public static final String LDONG_REGN_CD = "46";
    public static final String LDONG_SIGNGU_CD = "710";

    private AdminIncentiveRequestFixture() {
    }

    public static AdminIncentiveRequest incentiveRequest() {
        return new AdminIncentiveRequest(TITLE, URL, DESCRIPTION, START_DATE, END_DATE, regions());
    }

    public static AdminIncentiveRequest updatedIncentiveRequest() {
        return new AdminIncentiveRequest(UPDATED_TITLE, UPDATED_URL, UPDATED_DESCRIPTION, UPDATED_START_DATE, null, regions());
    }

    public static List<AdminIncentiveRequest.RegionRequest> regions() {
        return List.of(new AdminIncentiveRequest.RegionRequest(LDONG_REGN_CD, LDONG_SIGNGU_CD));
    }
}
