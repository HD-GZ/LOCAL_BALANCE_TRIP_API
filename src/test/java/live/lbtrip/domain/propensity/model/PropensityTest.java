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
            Propensity propensity = Propensity.create(
                UserFixture.user(),
                PropensityFixture.preference(),
                PropensityFixture.valueConsumption()
            );

            assertThat(propensity.getUser().getEmail()).isEqualTo(UserFixture.EMAIL);
            assertThat(propensity.getPreference().getLocality()).isEqualTo(PropensityRequestFixture.LOCALITY);
            assertThat(propensity.getPreference().getFrugality()).isEqualTo(PropensityRequestFixture.FRUGALITY);
            assertThat(propensity.getPreference().getExperientiality()).isEqualTo(PropensityRequestFixture.EXPERIENTIALITY);
            assertThat(propensity.getPreference().getVitality()).isEqualTo(PropensityRequestFixture.VITALITY);
            assertThat(propensity.getPreference().getSociality()).isEqualTo(PropensityRequestFixture.SOCIALITY);
            assertThat(propensity.getValueConsumption().getAccommodation()).isEqualTo(PropensityRequestFixture.ACCOMMODATION);
            assertThat(propensity.getValueConsumption().getFood()).isEqualTo(PropensityRequestFixture.FOOD);
            assertThat(propensity.getValueConsumption().getExperience()).isEqualTo(PropensityRequestFixture.EXPERIENCE);
            assertThat(propensity.getValueConsumption().getTransportation()).isEqualTo(PropensityRequestFixture.TRANSPORTATION);
            assertThat(propensity.getValueConsumption().getCafeExhibition()).isEqualTo(PropensityRequestFixture.CAFE_EXHIBITION);
        }
    }

    @Nested
    class 수정 {

        @Test
        void 취향_진단_점수를_갱신한다() {
            Propensity propensity = PropensityFixture.propensity();

            propensity.update(PropensityFixture.updatedPreference(), PropensityFixture.updatedValueConsumption());

            assertThat(propensity.getPreference().getLocality()).isEqualTo(PropensityRequestFixture.UPDATED_LOCALITY);
            assertThat(propensity.getPreference().getFrugality()).isEqualTo(PropensityRequestFixture.UPDATED_FRUGALITY);
            assertThat(propensity.getPreference().getExperientiality()).isEqualTo(PropensityRequestFixture.UPDATED_EXPERIENTIALITY);
            assertThat(propensity.getPreference().getVitality()).isEqualTo(PropensityRequestFixture.UPDATED_VITALITY);
            assertThat(propensity.getPreference().getSociality()).isEqualTo(PropensityRequestFixture.UPDATED_SOCIALITY);
            assertThat(propensity.getValueConsumption().getAccommodation()).isEqualTo(PropensityRequestFixture.UPDATED_ACCOMMODATION);
            assertThat(propensity.getValueConsumption().getFood()).isEqualTo(PropensityRequestFixture.UPDATED_FOOD);
            assertThat(propensity.getValueConsumption().getExperience()).isEqualTo(PropensityRequestFixture.UPDATED_EXPERIENCE);
            assertThat(propensity.getValueConsumption().getTransportation()).isEqualTo(PropensityRequestFixture.UPDATED_TRANSPORTATION);
            assertThat(propensity.getValueConsumption().getCafeExhibition()).isEqualTo(PropensityRequestFixture.UPDATED_CAFE_EXHIBITION);
        }
    }
}
