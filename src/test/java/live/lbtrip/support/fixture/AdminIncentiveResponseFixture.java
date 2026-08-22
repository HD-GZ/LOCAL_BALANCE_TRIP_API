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
            AdminIncentiveRequestFixture.TITLE_EN,
            AdminIncentiveRequestFixture.URL,
            AdminIncentiveRequestFixture.DESCRIPTION,
            AdminIncentiveRequestFixture.DESCRIPTION_EN,
            AdminIncentiveRequestFixture.START_DATE,
            AdminIncentiveRequestFixture.END_DATE,
            regionResponses()
        );
    }

    public static AdminIncentiveResponse updatedIncentiveResponse() {
        return new AdminIncentiveResponse(
            INCENTIVE_ID,
            AdminIncentiveRequestFixture.UPDATED_TITLE,
            AdminIncentiveRequestFixture.UPDATED_TITLE_EN,
            AdminIncentiveRequestFixture.UPDATED_URL,
            AdminIncentiveRequestFixture.UPDATED_DESCRIPTION,
            AdminIncentiveRequestFixture.UPDATED_DESCRIPTION_EN,
            AdminIncentiveRequestFixture.UPDATED_START_DATE,
            null,
            regionResponses()
        );
    }

    public static AdminIncentiveResponse legacyIncentiveResponse() {
        return new AdminIncentiveResponse(
            INCENTIVE_ID,
            AdminIncentiveRequestFixture.TITLE,
            null,
            AdminIncentiveRequestFixture.URL,
            AdminIncentiveRequestFixture.DESCRIPTION,
            null,
            null,
            null,
            regionResponses()
        );
    }

    private static List<AdminIncentiveResponse.RegionResponse> regionResponses() {
        return List.of(new AdminIncentiveResponse.RegionResponse(
            RegionCandidateFixture.CANDIDATE_ID, RegionCandidateFixture.NAME));
    }

    public static List<AdminIncentiveResponse> incentiveResponses() {
        return List.of(incentiveResponse());
    }
}
