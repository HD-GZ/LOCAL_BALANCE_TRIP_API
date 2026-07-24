package live.lbtrip.support.fixture;

import java.util.List;

import live.lbtrip.domain.incentive.dto.response.IncentiveResponse;

public final class IncentiveResponseFixture {

    public static final long INCENTIVE_ID = 1L;

    private IncentiveResponseFixture() {
    }

    public static IncentiveResponse incentiveResponse() {
        return new IncentiveResponse(
            INCENTIVE_ID,
            IncentiveRequestFixture.TITLE,
            IncentiveRequestFixture.URL,
            IncentiveRequestFixture.DESCRIPTION,
            regionResponses()
        );
    }

    public static IncentiveResponse updatedIncentiveResponse() {
        return new IncentiveResponse(
            INCENTIVE_ID,
            IncentiveRequestFixture.UPDATED_TITLE,
            IncentiveRequestFixture.UPDATED_URL,
            IncentiveRequestFixture.UPDATED_DESCRIPTION,
            regionResponses()
        );
    }

    private static List<IncentiveResponse.RegionResponse> regionResponses() {
        return List.of(new IncentiveResponse.RegionResponse(
            IncentiveRequestFixture.LDONG_REGN_CD, IncentiveRequestFixture.LDONG_SIGNGU_CD));
    }

    public static List<IncentiveResponse> incentiveResponses() {
        return List.of(incentiveResponse());
    }
}
