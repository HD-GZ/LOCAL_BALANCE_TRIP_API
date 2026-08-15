package live.lbtrip.support.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import live.lbtrip.domain.region.model.RegionCandidate;

public final class RegionCandidateFixture {

    public static final long CANDIDATE_ID = 1L;
    public static final String NAME = "전라남도 담양군";
    public static final String LDONG_REGN_CD = "46";
    public static final String LDONG_SIGNGU_CD = "710";

    private RegionCandidateFixture() {
    }

    public static RegionCandidate candidate() {
        return RegionCandidate.create(NAME, LDONG_REGN_CD, LDONG_SIGNGU_CD);
    }

    public static RegionCandidate candidateWithId() {
        RegionCandidate candidate = candidate();
        ReflectionTestUtils.setField(candidate, "id", CANDIDATE_ID);
        return candidate;
    }
}
