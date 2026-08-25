package live.lbtrip.domain.region.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.support.fixture.RegionCandidateFixture;

class RegionGreenMetricsTest {

    @Nested
    class 그린_점수_계산 {

        @Test
        void 그린_점수는_참인_신호의_개수다() {
            RegionGreenMetrics metrics = RegionGreenMetrics.create(
                RegionCandidateFixture.candidate(), true, true, false, false, true);

            assertThat(metrics.greenScore()).isEqualTo(3);
        }

        @Test
        void 모든_신호가_거짓이면_그린_점수는_0이다() {
            RegionGreenMetrics metrics = RegionGreenMetrics.create(
                RegionCandidateFixture.candidate(), false, false, false, false, false);

            assertThat(metrics.greenScore()).isZero();
        }

        @Test
        void 모든_신호가_참이면_그린_점수는_5다() {
            RegionGreenMetrics metrics = RegionGreenMetrics.create(
                RegionCandidateFixture.candidate(), true, true, true, true, true);

            assertThat(metrics.greenScore()).isEqualTo(5);
        }
    }

    @Nested
    class gpx_인접_갱신 {

        @Test
        void gpx_인접_여부를_갱신하면_그린_점수에_반영된다() {
            RegionGreenMetrics metrics = RegionGreenMetrics.create(
                RegionCandidateFixture.candidate(), false, false, false, false, true);

            metrics.updateGpxAdjacent(true);

            assertThat(metrics.isGpxAdjacent()).isTrue();
            assertThat(metrics.greenScore()).isEqualTo(2);
        }
    }
}
