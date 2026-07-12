package live.lbtrip.support.fixture;

import java.util.List;

import live.lbtrip.domain.admin.incentive.dto.response.IncentiveResponse;

public final class IncentiveResponseFixture {

    public static final long INCENTIVE_ID = 1L;

    private IncentiveResponseFixture() {
    }

    public static IncentiveResponse incentiveResponse() {
        return new IncentiveResponse(
            INCENTIVE_ID,
            IncentiveRequestFixture.TITLE,
            IncentiveRequestFixture.URL
        );
    }

    public static IncentiveResponse updatedIncentiveResponse() {
        return new IncentiveResponse(
            INCENTIVE_ID,
            IncentiveRequestFixture.UPDATED_TITLE,
            IncentiveRequestFixture.UPDATED_URL
        );
    }

    public static List<IncentiveResponse> incentiveResponses() {
        return List.of(incentiveResponse());
    }
}
