package live.lbtrip.domain.propensity.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PropensityBucketTest {

    @Nested
    class 점수_변환 {

        @Test
        void 일이와_이는_LOW로_변환한다() {
            assertThat(PropensityBucket.fromScore(1)).isEqualTo(PropensityBucket.LOW);
            assertThat(PropensityBucket.fromScore(2)).isEqualTo(PropensityBucket.LOW);
        }

        @Test
        void 삼은_NEUTRAL로_변환한다() {
            assertThat(PropensityBucket.fromScore(3)).isEqualTo(PropensityBucket.NEUTRAL);
        }

        @Test
        void 사와_오는_HIGH로_변환한다() {
            assertThat(PropensityBucket.fromScore(4)).isEqualTo(PropensityBucket.HIGH);
            assertThat(PropensityBucket.fromScore(5)).isEqualTo(PropensityBucket.HIGH);
        }
    }
}
