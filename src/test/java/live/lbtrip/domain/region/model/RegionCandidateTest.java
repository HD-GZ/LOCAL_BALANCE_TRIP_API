package live.lbtrip.domain.region.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RegionCandidateTest {

    @Test
    void 지역_후보를_생성한다() {
        RegionCandidate candidate = RegionCandidate.create("전라남도 담양군", "46", "710");

        assertThat(candidate.getName()).isEqualTo("전라남도 담양군");
        assertThat(candidate.getLdongRegnCd()).isEqualTo("46");
        assertThat(candidate.getLdongSignguCd()).isEqualTo("710");
    }
}
