package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WalkTimeCalculatorTest {

    @Nested
    class 거리_계산 {

        @Test
        void 같은_좌표의_거리는_0이다() {
            Double distance = WalkTimeCalculator.distanceMeters(126.981, 35.321, 126.981, 35.321);

            assertThat(distance).isZero();
        }

        @Test
        void 좌표가_없으면_거리를_계산하지_않는다() {
            assertThat(WalkTimeCalculator.distanceMeters(null, 35.321, 126.981, 35.321)).isNull();
        }
    }

    @Nested
    class 도보_시간_계산 {

        @Test
        void 거리를_분당_67미터로_환산한다() {
            Integer minutes = WalkTimeCalculator.walkMinutes(126.981, 35.321, 126.981, 35.3216);

            assertThat(minutes).isEqualTo(1);
        }

        @Test
        void 같은_좌표도_최소_1분을_반환한다() {
            assertThat(WalkTimeCalculator.walkMinutes(126.981, 35.321, 126.981, 35.321)).isEqualTo(1);
        }
    }
}
