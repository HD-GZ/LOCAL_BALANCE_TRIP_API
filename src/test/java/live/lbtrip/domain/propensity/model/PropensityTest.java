package live.lbtrip.domain.propensity.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.support.fixture.PropensityFixture;
import live.lbtrip.support.fixture.PropensityRequestFixture;
import live.lbtrip.support.fixture.UserFixture;

class PropensityTest {

    @Nested
    class 생성 {

        @Test
        void 취향_진단_결과를_생성한다() {
            Propensity propensity = Propensity.create(UserFixture.user(), PropensityFixture.scores());

            assertThat(propensity.getUser().getEmail()).isEqualTo(UserFixture.EMAIL);
            assertThat(propensity.getLocality()).isEqualTo(PropensityRequestFixture.LOCALITY);
            assertThat(propensity.getFrugality()).isEqualTo(PropensityRequestFixture.FRUGALITY);
            assertThat(propensity.getFlexibility()).isEqualTo(PropensityRequestFixture.FLEXIBILITY);
            assertThat(propensity.getExperientiality()).isEqualTo(PropensityRequestFixture.EXPERIENTIALITY);
            assertThat(propensity.getVitality()).isEqualTo(PropensityRequestFixture.VITALITY);
            assertThat(propensity.getSociality()).isEqualTo(PropensityRequestFixture.SOCIALITY);
        }
    }

    @Nested
    class 수정 {

        @Test
        void 취향_진단_점수를_갱신한다() {
            Propensity propensity = PropensityFixture.propensity();

            propensity.updateScores(PropensityFixture.updatedScores());

            assertThat(propensity.getLocality()).isEqualTo(PropensityRequestFixture.UPDATED_LOCALITY);
            assertThat(propensity.getFrugality()).isEqualTo(PropensityRequestFixture.UPDATED_FRUGALITY);
            assertThat(propensity.getFlexibility()).isEqualTo(PropensityRequestFixture.UPDATED_FLEXIBILITY);
            assertThat(propensity.getExperientiality()).isEqualTo(PropensityRequestFixture.UPDATED_EXPERIENTIALITY);
            assertThat(propensity.getVitality()).isEqualTo(PropensityRequestFixture.UPDATED_VITALITY);
            assertThat(propensity.getSociality()).isEqualTo(PropensityRequestFixture.UPDATED_SOCIALITY);
        }
    }
}
