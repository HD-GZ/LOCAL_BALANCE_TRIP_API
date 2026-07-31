package live.lbtrip.domain.propensity.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TravelProfileCodeTest {

    @Nested
    class 코드_산출 {

        @Test
        void 모든_점수가_최저면_앞극_코드를_만든다() {
            TravelProfileCode code = TravelProfileCode.from(Preference.of(1, 1, 1, 1, 1));

            assertThat(code.value()).isEqualTo("HPSRI");
        }

        @Test
        void 모든_점수가_최고면_뒷극_코드를_만든다() {
            TravelProfileCode code = TravelProfileCode.from(Preference.of(5, 5, 5, 5, 5));

            assertThat(code.value()).isEqualTo("LVEAG");
        }

        @Test
        void 삼점은_앞극으로_분류한다() {
            TravelProfileCode code = TravelProfileCode.from(Preference.of(3, 3, 3, 3, 3));

            assertThat(code.value()).isEqualTo("HPSRI");
        }

        @Test
        void 사점은_뒷극으로_분류한다() {
            TravelProfileCode code = TravelProfileCode.from(Preference.of(4, 4, 4, 4, 4));

            assertThat(code.value()).isEqualTo("LVEAG");
        }

        @Test
        void 축마다_독립적으로_극을_판정한다() {
            TravelProfileCode code = TravelProfileCode.from(Preference.of(4, 3, 4, 3, 4));

            assertThat(code.value()).isEqualTo("LPERG");
        }
    }
}
