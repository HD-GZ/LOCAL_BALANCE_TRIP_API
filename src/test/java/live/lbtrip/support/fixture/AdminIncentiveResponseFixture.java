package live.lbtrip.support.fixture;

import java.util.List;

import live.lbtrip.admin.incentive.dto.response.AdminIncentiveResponse;

public final class AdminIncentiveResponseFixture {

    public static final long INCENTIVE_ID = 1L;

    private AdminIncentiveResponseFixture() {
    }

    public static AdminIncentiveResponse incentiveResponse() {
        return new AdminIncentiveResponse(
            INCENTIVE_ID,
            AdminIncentiveRequestFixture.TITLE,
            AdminIncentiveRequestFixture.URL,
            AdminIncentiveRequestFixture.DESCRIPTION,
            AdminIncentiveRequestFixture.START_DATE,
            AdminIncentiveRequestFixture.END_DATE,
            regionResponses()
        );
    }

    public static AdminIncentiveResponse updatedIncentiveResponse() {
        return new AdminIncentiveResponse(
            INCENTIVE_ID,
            AdminIncentiveRequestFixture.UPDATED_TITLE,
            AdminIncentiveRequestFixture.UPDATED_URL,
            AdminIncentiveRequestFixture.UPDATED_DESCRIPTION,
            AdminIncentiveRequestFixture.UPDATED_START_DATE,
            null,
            regionResponses()
        );
    }

    public static AdminIncentiveResponse legacyIncentiveResponse() {
        return new AdminIncentiveResponse(
            INCENTIVE_ID,
            AdminIncentiveRequestFixture.TITLE,
            AdminIncentiveRequestFixture.URL,
            AdminIncentiveRequestFixture.DESCRIPTION,
            null,
            AdminIncentiveRequestFixture.END_DATE,
            regionResponses()
        );
    }

    private static List<AdminIncentiveResponse.RegionResponse> regionResponses() {
        return List.of(new AdminIncentiveResponse.RegionResponse(
            AdminIncentiveRequestFixture.LDONG_REGN_CD, AdminIncentiveRequestFixture.LDONG_SIGNGU_CD));
    }

    public static List<AdminIncentiveResponse> incentiveResponses() {
        return List.of(incentiveResponse());
    }
}
