package live.lbtrip.support.fixture;

import java.time.LocalDate;
import java.util.List;

import live.lbtrip.admin.incentive.dto.request.AdminIncentiveRequest;

public final class AdminIncentiveRequestFixture {

    public static final String TITLE = "KTX 인구감소지역 할인";
    public static final String TITLE_EN = "KTX Discount for Depopulation Areas";
    public static final String URL = "https://www.letskorail.com/event/discount";
    public static final String DESCRIPTION = "코레일 공식 채널로 이동";
    public static final String DESCRIPTION_EN = "Opens the official Korail channel";
    public static final String UPDATED_TITLE = "디지털 관광주민증";
    public static final String UPDATED_TITLE_EN = "Digital Tourist Residence Card";
    public static final String UPDATED_URL = "https://dtrc.visitkorea.or.kr";
    public static final String UPDATED_DESCRIPTION = "담양 가맹점 12곳 할인";
    public static final String UPDATED_DESCRIPTION_EN = "Discounts at 12 partner stores in Damyang";
    public static final LocalDate START_DATE = LocalDate.of(2026, 7, 1);
    public static final LocalDate END_DATE = LocalDate.of(2026, 8, 31);
    public static final LocalDate UPDATED_START_DATE = LocalDate.of(2026, 9, 1);

    private AdminIncentiveRequestFixture() {
    }

    public static AdminIncentiveRequest incentiveRequest() {
        return new AdminIncentiveRequest(TITLE, TITLE_EN, URL, DESCRIPTION, DESCRIPTION_EN,
            START_DATE, END_DATE, regionCandidateIds());
    }

    public static AdminIncentiveRequest updatedIncentiveRequest() {
        return new AdminIncentiveRequest(UPDATED_TITLE, UPDATED_TITLE_EN, UPDATED_URL,
            UPDATED_DESCRIPTION, UPDATED_DESCRIPTION_EN, UPDATED_START_DATE, null, regionCandidateIds());
    }

    public static List<Long> regionCandidateIds() {
        return List.of(RegionCandidateFixture.CANDIDATE_ID);
    }
}
