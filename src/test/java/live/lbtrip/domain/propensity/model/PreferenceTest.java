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
}
