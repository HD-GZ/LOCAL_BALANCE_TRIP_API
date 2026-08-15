package live.lbtrip.domain.propensity.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.support.fixture.PropensityRequestFixture;

class PreferenceTest {

    @Nested
    class 변환 {

        @Test
        void 요청에서_취향_진단_값_객체로_변환한다() {
            Preference preference = PropensityRequestFixture.propensityRequest().toPreference();

            assertThat(preference.getLocality()).isEqualTo(PropensityRequestFixture.LOCALITY);
            assertThat(preference.getFrugality()).isEqualTo(PropensityRequestFixture.FRUGALITY);
            assertThat(preference.getExperientiality()).isEqualTo(PropensityRequestFixture.EXPERIENTIALITY);
            assertThat(preference.getVitality()).isEqualTo(PropensityRequestFixture.VITALITY);
            assertThat(preference.getSociality()).isEqualTo(PropensityRequestFixture.SOCIALITY);
        }
    }

    @Nested
    class 여행_프로필_코드_산출 {

        @Test
        void 모든_점수가_최저면_앞극_코드를_만든다() {
            String code = Preference.of(1, 1, 1, 1, 1).toTravelProfileCode();

            assertThat(code).isEqualTo("HPSRI");
        }

        @Test
        void 모든_점수가_최고면_뒷극_코드를_만든다() {
            String code = Preference.of(5, 5, 5, 5, 5).toTravelProfileCode();

            assertThat(code).isEqualTo("LVEAG");
        }

        @Test
        void 삼점은_앞극으로_분류한다() {
            String code = Preference.of(3, 3, 3, 3, 3).toTravelProfileCode();

            assertThat(code).isEqualTo("HPSRI");
        }

        @Test
        void 사점은_뒷극으로_분류한다() {
            String code = Preference.of(4, 4, 4, 4, 4).toTravelProfileCode();

            assertThat(code).isEqualTo("LVEAG");
        }

        @Test
        void 축마다_독립적으로_극을_판정한다() {
            String code = Preference.of(4, 3, 4, 3, 4).toTravelProfileCode();

            assertThat(code).isEqualTo("LPERG");
        }
    }
}
