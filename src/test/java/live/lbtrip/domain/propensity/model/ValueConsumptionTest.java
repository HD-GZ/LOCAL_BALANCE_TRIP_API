package live.lbtrip.domain.propensity.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.support.fixture.PropensityRequestFixture;

class ValueConsumptionTest {

    @Nested
    class 변환 {

        @Test
        void 요청에서_가치소비_값_객체로_변환한다() {
            ValueConsumption valueConsumption = PropensityRequestFixture.propensityRequest().toValueConsumption();

            assertThat(valueConsumption.getAccommodation()).isEqualTo(PropensityRequestFixture.ACCOMMODATION);
            assertThat(valueConsumption.getFood()).isEqualTo(PropensityRequestFixture.FOOD);
            assertThat(valueConsumption.getExperience()).isEqualTo(PropensityRequestFixture.EXPERIENCE);
            assertThat(valueConsumption.getTransportation()).isEqualTo(PropensityRequestFixture.TRANSPORTATION);
            assertThat(valueConsumption.getCafeExhibition()).isEqualTo(PropensityRequestFixture.CAFE_EXHIBITION);
        }
    }
}
